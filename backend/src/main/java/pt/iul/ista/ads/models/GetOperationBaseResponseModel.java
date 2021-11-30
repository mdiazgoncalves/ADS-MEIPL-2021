package pt.iul.ista.ads.models;

public class GetOperationBaseResponseModel<T> {
	
	private String branch;
	private String latestCommit;
	
	protected T data;

	public String getBranch() {
		return branch;
	}

	public void setBranch(String branch) {
		this.branch = branch;
	}

	public String getLatestCommit() {
		return latestCommit;
	}

	public void setLatestCommit(String latestCommit) {
		this.latestCommit = latestCommit;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}
}
