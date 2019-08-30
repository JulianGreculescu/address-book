package au.com.gritmed.addressbook.dao;

import au.com.gritmed.addressbook.domain.AddressBookEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddressBookRepository extends JpaRepository<AddressBookEntry, Long> {
    List<AddressBookEntry> findByOwner(String owner);

    @Query(value =
            "SELECT id, first_name, last_name, phone_no, owner"
          + " FROM ("
          +  "SELECT first_name, last_name, phone_no, max(id) AS id, max(owner) AS owner, count(*)"
          + "  FROM address_books"
          + " WHERE owner IN :owners"
          + " GROUP BY first_name, last_name, phone_no "
          + "HAVING count(*) = 1)", nativeQuery = true)
    List<AddressBookEntry> findUniqueForOwners(@Param("owners") String[] owners);
}
