package hu.tiborpapp.epizodez;
import java.io.Serializable;

@SuppressWarnings("serial")
public class Episode implements Serializable {
	String tvID, title, seasonno, episode_title, aired;
	
	public Episode(String tvID, String title, String seasonno,
			String episode_title, String aired) {
		super();
		this.tvID = tvID;
		this.title = title;
		this.seasonno = seasonno;
		this.episode_title = episode_title;
		this.aired = aired;
	}
	
	public String getTvID() {
		return tvID;
	}

	public void setTvID(String tvID) {
		this.tvID = tvID;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSeasonno() {
		return seasonno;
	}

	public void setSeasonno(String seasonno) {
		this.seasonno = seasonno;
	}

	public String getEpisode_title() {
		return episode_title;
	}

	public void setEpisode_title(String episode_title) {
		this.episode_title = episode_title;
	}

	public String getAired() {
		return aired;
	}

	public void setAired(String aired) {
		this.aired = aired;
	}

	@Override
	public String toString() {
		return  title + " " + seasonno + " " + episode_title + " \n " + aired;
	}
	
}
