 package models;
 
 import java.util.Date;
 
 import javax.persistence.Entity;
 
 import play.data.validation.Required;
 import play.db.jpa.Blob;
 import play.db.jpa.Model;
 
 @Entity
 public class Version extends Model {
 
     @Required
     public String version;
 
     public Date publicationDate;
 
     public boolean isDefault;
 
     @Required
     public String matches;
 
    @Required
     public Blob artefact;
    
    public void dd1() {
   	 System.out.println("dddd");
   	 
   	 System.out.println("dddd111" + matches);
    }
 }
