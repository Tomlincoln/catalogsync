package hu.tomlincoln.catalogsync.repository;

import hu.tomlincoln.catalogsync.domain.Product;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Set;

public interface ProductRepository extends CrudRepository<Product, String> {

    @Query(value = "SELECT p.id FROM Product p WHERE p.id IN :ids")
    Set<String> getIdsWhereExists(Set<String> ids);

    @Query(value = "SELECT p FROM Product p WHERE p.id IN :ids")
    Set<Product> getAllWhereExists(Set<String> ids);

    @Query(value = "SELECT p.id FROM Product p WHERE p.id NOT IN :ids")
    Set<String> getIdsWhereNotExists(Set<String> ids);

}
