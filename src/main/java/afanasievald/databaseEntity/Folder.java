package afanasievald.databaseEntity;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
public class Folder {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true, nullable = false)
    private Integer id;

    @Column(unique = true)
    private String name;

    @Column
    private Date createdDate;

    @OneToMany(mappedBy = "folder")
    private List<Photo> photos = new ArrayList<Photo>();

    public Folder() {
    }

    public Folder(String name){
        this.name = name;
        this.createdDate = new Date();
    }

    public Folder(String name, Date createdDate){
        this.name = name;
        this.createdDate = createdDate;
    }

    public Integer getId() {
        return id;
    }

    public List<Photo> getPhotos() {
        return photos;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }
}

