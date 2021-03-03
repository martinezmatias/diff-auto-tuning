 /*-
  * Copyright (c) 2008, Derek Konigsberg
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  * 1. Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in the
  *    documentation and/or other materials provided with the distribution.
  * 3. Neither the name of the project nor the names of its
  *    contributors may be used to endorse or promote products derived
  *    from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
  * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
  * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
  * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
  * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
  * OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package org.logicprobe.LogicMail.ui;
 
 import java.util.Calendar;
 
 import net.rim.device.api.ui.Field;
 import net.rim.device.api.ui.Font;
 import net.rim.device.api.ui.Keypad;
 import net.rim.device.api.ui.Manager;
 import net.rim.device.api.ui.MenuItem;
 import net.rim.device.api.ui.Screen;
 import net.rim.device.api.ui.UiApplication;
 import net.rim.device.api.ui.component.AutoTextEditField;
 import net.rim.device.api.ui.component.Dialog;
 import net.rim.device.api.ui.component.Menu;
 import net.rim.device.api.util.Arrays;
 
 import org.logicprobe.LogicMail.LogicMailResource;
 import org.logicprobe.LogicMail.conf.AccountConfig;
 import org.logicprobe.LogicMail.conf.IdentityConfig;
 import org.logicprobe.LogicMail.conf.MailSettings;
 import org.logicprobe.LogicMail.message.Message;
 import org.logicprobe.LogicMail.message.MimeMessageContent;
 import org.logicprobe.LogicMail.message.MimeMessageContentFactory;
 import org.logicprobe.LogicMail.message.MessageEnvelope;
 import org.logicprobe.LogicMail.message.MessageFlags;
 import org.logicprobe.LogicMail.message.MimeMessagePart;
 import org.logicprobe.LogicMail.message.MimeMessagePartFactory;
 import org.logicprobe.LogicMail.message.TextContent;
 import org.logicprobe.LogicMail.message.TextPart;
 import org.logicprobe.LogicMail.message.UnsupportedContentException;
 import org.logicprobe.LogicMail.model.AccountNode;
 import org.logicprobe.LogicMail.model.Address;
 import org.logicprobe.LogicMail.model.MailboxNode;
 import org.logicprobe.LogicMail.model.MessageNode;
 import org.logicprobe.LogicMail.model.MessageNodeEvent;
 import org.logicprobe.LogicMail.model.MessageNodeListener;
 import org.logicprobe.LogicMail.util.EventObjectRunnable;
 import org.logicprobe.LogicMail.util.UnicodeNormalizer;
 
 
 /**
  * This is the message composition screen.
  */
 public class CompositionScreen extends AbstractScreenProvider {
     public final static int COMPOSE_NORMAL = 0;
     public final static int COMPOSE_REPLY = 1;
     public final static int COMPOSE_REPLY_ALL = 2;
     public final static int COMPOSE_FORWARD = 3;
     
     private int composeType = -1;
     private MessageNode sourceMessageNode;
     private AccountNode accountNode;
     private AccountConfig accountConfig;
     private UnicodeNormalizer unicodeNormalizer;
     
     private Screen screen;
     private BorderedFieldManager recipientsFieldManager;
 	private BorderedFieldManager subjectFieldManager;
 	private BorderedFieldManager messageFieldManager;
     private AutoTextEditField subjectEditField;
     private AutoTextEditField messageEditField;
 
     private String inReplyTo;
     private boolean messageSent;
     private IdentityConfig identityConfig;
     private MessageNode replyToMessageNode;
     
     private MenuItem sendMenuItem = new MenuItem(resources.getString(LogicMailResource.MENUITEM_SEND), 300100, 10) {
         public void run() {
             sendMessage();
         }
     };
     private MenuItem saveDraftMenuItem = new MenuItem(resources.getString(LogicMailResource.MENUITEM_SAVE_DRAFT), 300200, 20) {
         public void run() {
             saveAsDraft();
             screen.close();
         }
     };
     private MenuItem addToMenuItem = new MenuItem(resources.getString(LogicMailResource.MENUITEM_ADD_TO), 400100, 1010) {
         public void run() {
             insertRecipientField(EmailAddressBookEditField.ADDRESS_TO);
         }
     };
 
     private MenuItem addCcMenuItem = new MenuItem(resources.getString(LogicMailResource.MENUITEM_ADD_CC), 400200, 1020) {
         public void run() {
             insertRecipientField(EmailAddressBookEditField.ADDRESS_CC);
         }
     };
 
     private MenuItem addBccMenuItem = new MenuItem(resources.getString(LogicMailResource.MENUITEM_ADD_BCC), 400300, 1030) {
         public void run() {
             insertRecipientField(EmailAddressBookEditField.ADDRESS_BCC);
         }
     };
 
     private MessageNodeListener messageNodeListener = new MessageNodeListener() {
         public void messageStatusChanged(MessageNodeEvent e) {
             messageNodeListener_MessageStatusChanged(e);
         }
     };
 
     /**
      * Creates a new instance of CompositionScreen.
      *
      * @param accountNode Account node
      */
     public CompositionScreen(AccountNode accountNode) {
         this.accountNode = accountNode;
         this.accountConfig = accountNode.getAccountConfig();
         this.identityConfig = accountConfig.getIdentityConfig();
         if(MailSettings.getInstance().getGlobalConfig().getUnicodeNormalization()) {
             unicodeNormalizer = UnicodeNormalizer.getInstance();
         }
     }
 
     /**
      * Creates a new instance of CompositionScreen.
      *
      * @param accountNode Account node
      * @param recipient Message recipient address to pre-populate the "To" field with
      */
     public CompositionScreen(AccountNode accountNode, String recipient) {
     	this(accountNode);
 
     	EmailAddressBookEditField toAddressField = (EmailAddressBookEditField) recipientsFieldManager.getField(0);
     	toAddressField.setText(recipient);
     }
 
     /**
      * Creates a new instance of CompositionScreen.
      * Used for working with an already created message,
      * such as a draft, reply, or forward.
      *
      * @param accountNode Account node
      * @param messageNode Message we are composing
      * @param composeType Type of message we are creating
      */
     public CompositionScreen(
     		AccountNode accountNode,
     		MessageNode messageNode,
     		int composeType) {
         this.accountNode = accountNode;
         this.accountConfig = accountNode.getAccountConfig();
         this.identityConfig = accountConfig.getIdentityConfig();
         if(MailSettings.getInstance().getGlobalConfig().getUnicodeNormalization()) {
             unicodeNormalizer = UnicodeNormalizer.getInstance();
         }
 
         this.composeType = composeType;
         this.sourceMessageNode = messageNode;
     }
 
     private void messageNodeListener_MessageStatusChanged(MessageNodeEvent e) {
     	if(e.getType() == MessageNodeEvent.TYPE_STRUCTURE_LOADED) {
     		UiApplication.getUiApplication().invokeLater(new EventObjectRunnable(e) {
 				public void run() {
 			    	MessageNode messageNode = (MessageNode)getEvent().getSource();
 		    		messageNode.removeMessageNodeListener(messageNodeListener);
 		    		populateFromMessage(messageNode);
 		    		messageEditField.setEditable(true);
 				}
     		});
     	}
     }
 
     private void populateFromMessage(MessageNode message) {
         int i;
         MimeMessagePart body = message.getMessageStructure();
 
         // Currently only all-text reply bodies are supported
         if (body instanceof TextPart) {
         	MimeMessageContent content = message.getMessageContent(body);
         	if(content instanceof TextContent) {
 	            messageEditField.insert("\r\n");
 	            messageEditField.insert(normalize(((TextContent)content).getText()));
 	            messageEditField.setCursorPosition(0);
         	}
         }
 
         // Set the subject
         subjectEditField.setText(normalize(message.getSubject()));
 
         // Set the recipients
         Address[] recipients = message.getTo();
         if (recipients != null) {
             for (i = 0; i < recipients.length; i++) {
                 insertRecipientField(EmailAddressBookEditField.ADDRESS_TO).setText(normalize(recipients[i].toString()));
             }
         }
 
         recipients = message.getCc();
         if (recipients != null) {
             for (i = 0; i < recipients.length; i++) {
                 insertRecipientField(EmailAddressBookEditField.ADDRESS_CC).setText(normalize(recipients[i].toString()));
             }
         }
 
         recipients = message.getBcc();
         if (recipients != null) {
             for (i = 0; i < recipients.length; i++) {
                 insertRecipientField(EmailAddressBookEditField.ADDRESS_BCC).setText(normalize(recipients[i].toString()));
             }
         }
 
         inReplyTo = message.getInReplyTo();
     }
     
     private void appendSignature() {
         // Add the signature if available
         if (identityConfig != null) {
             String sig = identityConfig.getMsgSignature();
 
             if ((sig != null) && (sig.length() > 0)) {
                 messageEditField.insert("\r\n-- \r\n" + sig);
                 messageEditField.setCursorPosition(0);
             }
         }
     }
     
     public void initFields(Screen screen) {
         FieldFactory fieldFactory = FieldFactory.getInstance();
     	recipientsFieldManager = fieldFactory.getBorderedFieldManager(
         		Manager.NO_HORIZONTAL_SCROLL
         		| Manager.NO_VERTICAL_SCROLL
         		| BorderedFieldManager.BOTTOM_BORDER_NONE);
         recipientsFieldManager.add(new EmailAddressBookEditField(
                 EmailAddressBookEditField.ADDRESS_TO, ""));
         recipientsFieldManager.add(new EmailAddressBookEditField(
                 EmailAddressBookEditField.ADDRESS_CC, ""));
 
         subjectFieldManager = fieldFactory.getBorderedFieldManager(
         		Manager.NO_HORIZONTAL_SCROLL
         		| Manager.NO_VERTICAL_SCROLL
         		| BorderedFieldManager.BOTTOM_BORDER_NONE);
         subjectEditField = new AutoTextEditField(resources.getString(LogicMailResource.MESSAGEPROPERTIES_SUBJECT) + ' ', "");
         subjectEditField.setFont(subjectEditField.getFont().derive(Font.BOLD));
         subjectFieldManager.add(subjectEditField);
         
         messageFieldManager = new BorderedFieldManager(BorderedFieldManager.BOTTOM_BORDER_NORMAL | Field.USE_ALL_HEIGHT);
         messageEditField = new AutoTextEditField();
 		messageEditField.setEditable(false);
         messageFieldManager.add(messageEditField);
         
         screen.add(recipientsFieldManager);
         screen.add(subjectFieldManager);
         screen.add(messageFieldManager);
         
         if(sourceMessageNode == null) {
             appendSignature();
     		messageEditField.setEditable(true);
         }
         else if(composeType == COMPOSE_NORMAL) {
         	if(sourceMessageNode.getMessageStructure() != null) {
         		populateFromMessage(sourceMessageNode);
         		messageEditField.setEditable(true);
         	}
         	else {
         		sourceMessageNode.addMessageNodeListener(messageNodeListener);
         		sourceMessageNode.refreshMessage();
         	}
         }
         else
         {
 	        this.replyToMessageNode = sourceMessageNode;
 	
 	        MessageNode populateMessage;
 	
 	        switch (composeType) {
 	        case COMPOSE_REPLY:
 	        	populateMessage = sourceMessageNode.toReplyMessage();
 	            break;
 	        case COMPOSE_REPLY_ALL:
 	        	populateMessage = sourceMessageNode.toReplyAllMessage(identityConfig.getEmailAddress());
 	            break;
 	        case COMPOSE_FORWARD:
 	        	populateMessage = sourceMessageNode.toForwardMessage();
 	            break;
             default:
             	populateMessage = sourceMessageNode;
             	break;
 	        }
 	        populateFromMessage(populateMessage);
 	        appendSignature();
     		messageEditField.setEditable(true);
         }
         
         this.screen = screen;
     }
 
     public boolean onClose() {
         if (!messageSent &&
                 ((subjectEditField.getText().length() > 0) ||
                 (messageEditField.getText().length() > 0))) {
 
         	boolean shouldClose = false;
         	if(accountConfig.getDraftMailbox() != null) {
         		int choice = Dialog.ask(
         				resources.getString(LogicMailResource.COMPOSITION_PROMPT_SAVE_OR_DISCARD),
         				new Object[] {
         						resources.getString(LogicMailResource.MENUITEM_SAVE_AS_DRAFT),
         						resources.getString(LogicMailResource.MENUITEM_DISCARD),
         						resources.getString(LogicMailResource.MENUITEM_CANCEL) }, 0);
         		if(choice == 0) {
         			// Save as draft, then close
         			saveAsDraft();
         			shouldClose = true;
         		}
         		else if(choice == 1) {
         			shouldClose = true;
         		}
         	}
         	else {
         		int choice =
         			Dialog.ask(
         					resources.getString(LogicMailResource.COMPOSITION_PROMPT_DISCARD_UNSENT),
         					new Object[] {
         							resources.getString(LogicMailResource.MENUITEM_DISCARD),
         							resources.getString(LogicMailResource.MENUITEM_CANCEL)}, 0);
         		if(choice == 0) { shouldClose = true; }
         	}
         	
             if (shouldClose) {
             	screen.close();
                 return true;
             } else {
                 return false;
             }
         } else {
         	screen.close();
             return true;
         }
     }
 
     private void saveAsDraft() {
         MailboxNode draftMailbox = accountConfig.getDraftMailbox();
         MessageEnvelope envelope = generateEnvelope();
         Message message = generateMessage();
         
         envelope.date = Calendar.getInstance().getTime();
         MessageFlags messageFlags = new MessageFlags(
         		false,
         		false,
         		false,
         		false,
         		true,
         		true,
         		false);
         draftMailbox.appendMessage(envelope, message, messageFlags);
         // TODO: Save reply-to information with the draft message
     }
 
     public void makeMenu(Menu menu, int instance) {
         if (((EmailAddressBookEditField) recipientsFieldManager.getField(0))
                 .getText().length() > 0) {
             menu.add(sendMenuItem);
         }
         MailboxNode draftMailbox = accountConfig.getDraftMailbox();
         if(draftMailbox != null
                 && ((subjectEditField.getText().length() > 0)
                 || (messageEditField.getText().length() > 0))) {
             menu.add(saveDraftMenuItem);
         }
 
         menu.add(addToMenuItem);
         menu.add(addCcMenuItem);
         menu.add(addBccMenuItem);
     }
 
     private MessageEnvelope generateEnvelope() {
         // Simplest possible implementation for now,
         // which turns the content of the screen into
         // a message containing a single text/plain section
         MessageEnvelope env = new MessageEnvelope();
 
         env.inReplyTo = inReplyTo;
 
         // Build the recipients list
         EmailAddressBookEditField currentField;
         int size = recipientsFieldManager.getFieldCount();
 
         for (int i = 0; i < size; i++) {
             currentField = (EmailAddressBookEditField) recipientsFieldManager.getField(i);
 
             if ((currentField.getAddressType() == EmailAddressBookEditField.ADDRESS_TO) &&
                     (currentField.getText().length() > 0)) {
                 if (env.to == null) {
                     env.to = new String[1];
                     env.to[0] = currentField.getText();
                 } else {
                     Arrays.add(env.to, currentField.getText());
                 }
             } else if ((currentField.getAddressType() == EmailAddressBookEditField.ADDRESS_CC) &&
                     (currentField.getText().length() > 0)) {
                 if (env.cc == null) {
                     env.cc = new String[1];
                     env.cc[0] = currentField.getText();
                 } else {
                     Arrays.add(env.cc, currentField.getText());
                 }
             } else if ((currentField.getAddressType() == EmailAddressBookEditField.ADDRESS_BCC) &&
                     (currentField.getText().length() > 0)) {
                 if (env.bcc == null) {
                     env.bcc = new String[1];
                     env.bcc[0] = currentField.getText();
                 } else {
                     Arrays.add(env.bcc, currentField.getText());
                 }
             }
         }
 
         // Set the sender and reply-to addresses
         // (this comes from identity settings)
         if (identityConfig != null) {
             env.from = new String[1];
 
             String fullName = identityConfig.getFullName();
 
             if ((fullName != null) && (fullName.length() > 0)) {
                 env.from[0] = "\"" + fullName + "\"" + " <" +
                     identityConfig.getEmailAddress() + ">";
             } else {
                 env.from[0] = identityConfig.getEmailAddress();
             }
 
             String replyToAddress = identityConfig.getReplyToAddress();
 
             if ((replyToAddress != null) && (replyToAddress.length() > 0)) {
                 env.replyTo = new String[1];
                 env.replyTo[0] = replyToAddress;
             }
         } else {
             // There are rare situations where the IdentityConfig could be null,
             // such as if the user deleted their identity configuration without
             // editing their account again to force the creation of a default identity.
             // Eventually this should be prevented, but for now we will just elegantly
             // handle the case of missing identity information.
             env.from = new String[1];
             env.from[0] = accountConfig.getServerUser() + "@" +
                 accountConfig.getServerName();
         }
 
         // Set the subject
         env.subject = subjectEditField.getText();
 
         return env;
     }
     
     private Message generateMessage() {
     	String contentText = messageEditField.getText();
         MimeMessagePart bodyPart = MimeMessagePartFactory.createMimeMessagePart(
         		"text", "plain", null, "7bit", "us-ascii", "", "", contentText.length());
         MimeMessageContent bodyContent;
         try {
 			bodyContent = MimeMessageContentFactory.createContent(
 					bodyPart, contentText);
 		} catch (UnsupportedContentException e) {
 			bodyContent = null;
 		}
         
         Message message = new Message(bodyPart);
         message.putContent(bodyPart, bodyContent);
     	return message;
     }
     
     private void sendMessage() {
     	MessageEnvelope envelope = generateEnvelope();
     	Message message = generateMessage();
     	
         if (replyToMessageNode != null) {
             accountNode.sendMessageReply(envelope, message, replyToMessageNode);
         } else {
             accountNode.sendMessage(envelope, message);
         }
 
         messageSent = true;
         screen.setDirty(false);
         screen.close();
     }
 
     /**
      * Insert a new recipient field.
      * @param addressType The type of address this field will hold
      * @return The newly added field
      */
     private EmailAddressBookEditField insertRecipientField(int addressType) {
         int size = recipientsFieldManager.getFieldCount();
         EmailAddressBookEditField currentField;
         int i;
 
         // If a field of this type already exists, and is empty, move
         // focus there instead of adding a new field
         for (i = 0; i < size; i++) {
             currentField = (EmailAddressBookEditField) recipientsFieldManager.getField(i);
 
             if ((currentField.getAddressType() == addressType) &&
                     (currentField.getText().length() == 0)) {
                 currentField.setFocus();
 
                 return currentField;
             }
         }
 
         // Otherwise, find the appropriate insertion point,
         // and add a new field, and give it focus
         if (addressType == EmailAddressBookEditField.ADDRESS_TO) {
             for (i = 0; i < size; i++) {
                 currentField = (EmailAddressBookEditField) recipientsFieldManager.getField(i);
 
                 if (currentField.getAddressType() != EmailAddressBookEditField.ADDRESS_TO) {
                     currentField = new EmailAddressBookEditField(EmailAddressBookEditField.ADDRESS_TO,
                             "");
                     recipientsFieldManager.insert(currentField, i);
                     currentField.setFocus();
 
                     return currentField;
                 }
             }
         } else if (addressType == EmailAddressBookEditField.ADDRESS_CC) {
             i = 0;
 
             while (i < size) {
                 currentField = (EmailAddressBookEditField) recipientsFieldManager.getField(i);
 
                 if ((currentField.getAddressType() == EmailAddressBookEditField.ADDRESS_TO) ||
                         (currentField.getAddressType() == EmailAddressBookEditField.ADDRESS_CC)) {
                     i++;
                 } else {
                     currentField = new EmailAddressBookEditField(EmailAddressBookEditField.ADDRESS_CC,
                             "");
                     recipientsFieldManager.insert(currentField, i);
                     currentField.setFocus();
 
                     return currentField;
                 }
             }
         }
 
         currentField = new EmailAddressBookEditField(addressType, "");
         recipientsFieldManager.add(currentField);
         currentField.setFocus();
 
         return currentField;
     }
 
     public boolean keyChar(char key, int status, int time) {
         EmailAddressBookEditField currentField;
         int index;
 
         switch (key) {
         case Keypad.KEY_BACKSPACE:
             currentField = (EmailAddressBookEditField) recipientsFieldManager.getFieldWithFocus();
 
             if (currentField == null) {
                 break;
             }
 
             if (recipientsFieldManager.getFieldWithFocusIndex() == 0) {
                 break;
             }
 
             if (currentField.getText().length() > 0) {
                 break;
             }
 
             index = currentField.getIndex();
             recipientsFieldManager.delete(currentField);
             recipientsFieldManager.getField(index - 1).setFocus();
 
             return true;
         }
 
         return super.keyChar(key, status, time);
     }
 
     /**
      * Run the Unicode normalizer on the provide string,
      * only if normalization is enabled in the configuration.
      * If normalization is disabled, this method returns
      * the input unmodified.
      * 
      * @param input Input string
      * @return Normalized string
      */
     private String normalize(String input) {
         if(unicodeNormalizer == null) {
             return input;
         }
         else {
             return unicodeNormalizer.normalize(input);
         }
     }
 }
