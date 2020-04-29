package afanasievald.databaseEntity;

import javax.persistence.*;
import java.util.Date;

@Entity
public class Photo {
    @Id
    @Column(unique = true, nullable = false)
    private long identifier;

    @ManyToOne
    private Folder folder;

    @Column
    private String name;

    @Column
    private String description;

    @Column
    private Date createdDate =  new Date();

    public Photo() {
    }

    public Photo(long identifier, Folder folder, String name, String description){
        this.identifier = identifier;
        this.folder = folder;
        this.name = name;
        this.description = description;
    }

    public Photo(long identifier, Folder folder, String name, String description, Date createdDate){
        this.identifier = identifier;
        this.folder = folder;
        this.name = name;
        this.description = description;
        this.createdDate = createdDate;
    }

    public long getIdentifier() {
        return identifier;
    }

    public void setIdentifier(long identifier) {
        this.identifier = identifier;
    }

    public Folder getFolder() {
        return folder;
    }

    public void setFolder(Folder folder) {
        this.folder = folder;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }
}
