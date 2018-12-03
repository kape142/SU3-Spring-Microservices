package service2;

public class Service2Bean {

	private String id;

	public Service2Bean() {
	}

	Service2Bean(String id) {
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
		return "Service2Bean [id=" + id + "]";
	}

}