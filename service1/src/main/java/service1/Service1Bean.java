package service1;

public class Service1Bean {

	private String id;

	public Service1Bean() {
	}

	Service1Bean(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "Service1Bean [id=" + id + "]";
	}

}