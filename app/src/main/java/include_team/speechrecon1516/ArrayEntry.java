package include_team.speechrecon1516;

import java.util.Date;

public class ArrayEntry {
    private String name;
    private boolean transcribed;
    Date date;

    public ArrayEntry(String cname, Date dd, boolean ctranscribed){
        name = cname;
        date = dd;
        transcribed = ctranscribed;
    }

    public String getName() {
        return name;
    }

    public Date getDate() {
        return date;
    }

    public boolean isTranscribed() {
        return transcribed;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTranscribed(boolean transcribed) {
        this.transcribed = transcribed;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}