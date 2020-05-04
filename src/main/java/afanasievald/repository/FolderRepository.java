package afanasievald.repository;

import afanasievald.databaseEntity.Folder;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.repository.CrudRepository;
import java.util.List;
import java.util.Optional;

public interface FolderRepository extends CrudRepository<Folder, Integer> {
    List<Folder> findByOrderByCreatedDateAsc();
    Optional<Folder> findByName(@NotNull String name);
}
