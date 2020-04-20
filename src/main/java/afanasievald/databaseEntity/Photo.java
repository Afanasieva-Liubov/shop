package afanasievald.databaseEntity;

import javax.persistence.*;
import java.util.Date;

@Entity
public class Photo {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true, nullable = false)
    private Integer id;

    @Column(unique = true)
    private Integer hashcode;

    @ManyToOne
    private Folder folder;

    @Column
    private String name;

    @Column
    private String description;

    @Column
    private Date createdDate;

    public Photo() {
    }

    public Photo(Integer hashcode, Folder folder,  String name, String description){
        this.hashcode = hashcode;
        this.folder = folder;
        this.name = name;
        this.description = description;
        this.createdDate = new Date();
    }

    public Photo(Integer hashcode, Folder folder,  String name, String description, Date createdDate){
        this.hashcode = hashcode;
        this.folder = folder;
        this.name = name;
        this.description = description;
        this.createdDate = createdDate;
    }

    public Integer getId() {
        return id;
    }

    public Integer getHashcode() {
        return hashcode;
    }

    public void setHashcode(Integer hashcode) {
        this.hashcode = hashcode;
    }

    public Folder getFolder() {
        return folder;
    }

    public void setIdFolder(Folder folder) {
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
