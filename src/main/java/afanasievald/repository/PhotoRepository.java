package afanasievald.repository;

import afanasievald.databaseEntity.Folder;
import afanasievald.databaseEntity.Photo;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.repository.CrudRepository;
import java.util.List;
import java.util.Optional;

public interface PhotoRepository extends CrudRepository<Photo, Integer> {
    List<Photo> findByFolder(@NotNull Folder folder);
    Optional<Photo> findByIdentifier(long identifier);
}
