 package agent;
 
 import java.util.ArrayList;
 import java.util.List;
 import jade.core.AID;
 import logic.Clause;
 import logic.Literal;
 
 /* klasa do trzymania informacji o sąsiadach */
 public class Acquaintance {
 	List<Integer> reasoningIDs;
 	public List<Integer> getReasoningIDs() {
 		return reasoningIDs;
 	}
 	public void setReasoningIDs(List<Integer> reasoningIDs) {
 		this.reasoningIDs = reasoningIDs;
 	}
 
 	List<AID> agenci;
 	List<List<Literal>> literaly;
 
 	public Acquaintance(String s) {
 		String[] ints = s.split(";");
 		for(String i: ints)
 			reasoningIDs.add(Integer.parseInt(i));
 	}
 	public List<AID> getAgenci() {
 		return agenci;
 	}
 
 	public Boolean isWholeClauseShared(Clause c) {
 		for(Literal inputLiteral: c.asLiterals()) {
 			for(List<Literal> agentLiterals: literaly)
 				for(Literal carriedLiteral: agentLiterals) {
 				if(!carriedLiteral.equalLetter(inputLiteral))
 					return false;
 			}
 		}
 		return true;
 	}
 
 	public List<AID> getSasiedzi(Literal lit) {
 		List<AID> sasiedzi = new ArrayList<AID>();
 		for (int i = 0; i < agenci.size(); i++) {
 			for (int j = 0; j < literaly.get(j).size(); j++) {
 				if (literaly.get(i).get(j).getName() == lit.getName()) {
 					sasiedzi.add(agenci.get(i));
 					j = literaly.get(i).size();
 				}
 			}
 		}
 		return sasiedzi;
 	}
 
 	public void update(AID agentId, Literal lit) {
 		boolean flaga = true;
 		for (int i = 0; i < agenci.size(); i++) {
 			if (agenci.get(i) == agentId) {
 				for (int j = 0; j < literaly.get(i).size(); j++) {
 					if (literaly.get(i).get(j).getName() == lit.getName()) {
 
 						literaly.get(i).add(lit);
 					}
 				}
 				flaga = false;
 			}
 		}
 		if (flaga) {
 			agenci.add(agentId);
 			List<Literal> tmp = new ArrayList<Literal>();
 			tmp.add(lit);
 			literaly.add(tmp);
 		}
 	}
 }
