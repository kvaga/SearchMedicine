
public class SearchMedicineException extends Exception{
	private SearchMedicineException(String message) {
		super(message);
	}
	
	public static class MedcineIsOutOfStock extends SearchMedicineException {
		public MedcineIsOutOfStock(String message) {
			super(message);
		}
	}
	
	public static class NoSitesAndCheckFound extends SearchMedicineException{
		public NoSitesAndCheckFound(String message) {
			super(message);
		}
	}
}
