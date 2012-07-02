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
}