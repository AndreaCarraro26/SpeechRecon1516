package include_team.speechrecon1516;

import java.util.Date;

/**
 * Item of RecyclerView.
 */
public class ArrayEntry {
    private String name;
    private boolean transcribed;
    private Date date;

    /**
     * Create an ArrayEntry
     * @param cname Name of the file
     * @param dd    last modify date
     * @param ctranscribed if has already been transcribed
     */
    public ArrayEntry(String cname, Date dd, boolean ctranscribed){
        name = cname;
        date = dd;
        transcribed = ctranscribed;
    }

    /**
     * @return file name
     */
    public String getName() {
        return name;
    }

    /**
     * @return last modify date with Date class
     */
    public Date getDate() {
        return date;
    }

    /**
     * @return if the file has already been transmitted
     */
    public boolean isTranscribed() {
        return transcribed;
    }

    /**
     * Set filename
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * set it file has been transcribed
     */
    public void setTranscribed() {
        this.transcribed = true;
    }

}