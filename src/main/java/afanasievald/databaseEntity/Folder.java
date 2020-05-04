package afanasievald.databaseEntity;

import org.jetbrains.annotations.NotNull;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
public class Folder {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true, nullable = false)
    private int id;

    @Column(unique = true)
    private String name;

    @Column
    @NotNull
    private Date createdDate = new Date();

    @OneToMany(mappedBy = "folder")
    private final List<Photo> photos = new ArrayList<>();

    public Folder() {
    }

    public Folder(@NotNull String name){
        this.name = name;
    }

    public Folder(@NotNull String name, @NotNull Date createdDate){
        this.name = name;
        this.createdDate = createdDate;
    }

    public int getId() {
        return id;
    }

    public List<Photo> getPhotos() {
        return photos;
    }

    public String getName() {
        return name;
    }

    public void setName(@NotNull String name) {
        this.name = name;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(@NotNull Date createdDate) {
        this.createdDate = createdDate;
    }
}

