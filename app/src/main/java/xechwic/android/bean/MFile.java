package xechwic.android.bean;

import java.util.ArrayList;

public class MFile {
	private String filePath;
	private String fileName;
	private ArrayList<MFile> files;
	private boolean isDirectory;
	private String parentFilePath;
	private boolean isBitmap=false;

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public ArrayList<MFile> getFiles() {
		return files;
	}

	public void setFiles(ArrayList<MFile> files) {
		this.files = files;
	}

	public boolean isDirectory() {
		return isDirectory;
	}

	public void setDirectory(boolean isDirectory) {
		this.isDirectory = isDirectory;
	}

	public String getParentFilePath() {
		return parentFilePath;
	}

	public void setParentFilePath(String parentFilePath) {
		this.parentFilePath = parentFilePath;
	}

	public boolean isBitmap() {
		return isBitmap;
	}

	public void setBitmap(boolean isBitmap) {
		this.isBitmap = isBitmap;
	}
}
