package tawusel.android.enums;

public enum TourKind {
	ACTIVE(1),
	TEMPLATE(2),
	AVAILABLE(3);
	
	private int tourKind;
	 
	 private TourKind(int i) {
	   tourKind = i;
	 }
	 
	 public int getTourKind() {
	   return tourKind;
	 }
	 
	 public static TourKind parseTourKind(String kindString) {
			if(kindString.equals("ACTIVE")) {
				return TourKind.ACTIVE;
			} else if(kindString.equals("TEMPLATE")) {
				return TourKind.TEMPLATE;
			} else {
				return TourKind.AVAILABLE;
			}
	}
}