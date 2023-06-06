package example.cashcard.controllers;

import example.cashcard.models.CashCard;
import example.cashcard.repositories.CashCardRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/cashcards")
public class CashCardController {

    public CashCardController(CashCardRepository cashCardRepository) {
        this.cashCardRepository = cashCardRepository;
    }

    private final CashCardRepository cashCardRepository;

    @GetMapping("/{requestId}")
    public ResponseEntity<CashCard> findById(@PathVariable Long requestId, Principal principal) {
        CashCard cashCard = this.findCashCard(requestId, principal);

        if (cashCard != null) {
            return ResponseEntity.ok(cashCard);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping()
    public ResponseEntity<Void> createCashCard(@RequestBody CashCard newCardCashRequest, UriComponentsBuilder ucb, Principal principal) {
        CashCard cashCardWithOwner = new CashCard(null, newCardCashRequest.amount(), principal.getName());

        CashCard savedCashCard = this.cashCardRepository.save(cashCardWithOwner);
        URI locationOfNewCashCard = ucb.path("cashcards/{id}").buildAndExpand(savedCashCard.id()).toUri();
        return ResponseEntity.created(locationOfNewCashCard).build();
    }

    @GetMapping()
    public ResponseEntity<List<CashCard>> findAll(Pageable pageable, Principal principal) {
        Page<CashCard> page = cashCardRepository.findByOwner(
                principal.getName(),
                PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        pageable.getSortOr(Sort.by(Sort.Direction.ASC, "amount"))
                ));

        return ResponseEntity.ok(page.getContent());
    }

    @PutMapping("/{requestId}")
    public ResponseEntity<Void> putCashCard(@PathVariable Long requestId, @RequestBody CashCard cashCardUpdate, Principal principal) {
        CashCard cashCard = this.findCashCard(requestId, principal);

        if (cashCard != null) {
            CashCard updatedCashCard = new CashCard(cashCard.id(), cashCardUpdate.amount(), principal.getName());
            this.cashCardRepository.save(updatedCashCard);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    private CashCard findCashCard(Long requestId, Principal principal) {
        return this.cashCardRepository.findByIdAndOwner(requestId, principal.getName());
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteCashCard(@PathVariable Long id, Principal principal) {
        if (this.cashCardRepository.existsByIdAndOwner(id, principal.getName())) {
            this.cashCardRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
