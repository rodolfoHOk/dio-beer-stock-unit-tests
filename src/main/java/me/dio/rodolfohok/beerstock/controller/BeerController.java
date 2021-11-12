package me.dio.rodolfohok.beerstock.controller;

import lombok.AllArgsConstructor;
import me.dio.rodolfohok.beerstock.dto.BeerDTO;
import me.dio.rodolfohok.beerstock.dto.QuantityDTO;
import me.dio.rodolfohok.beerstock.exception.BeerAlreadyRegisteredException;
import me.dio.rodolfohok.beerstock.exception.BeerNotFoundException;
import me.dio.rodolfohok.beerstock.exception.BeerStockExceededException;
import me.dio.rodolfohok.beerstock.service.BeerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/beers")
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class BeerController implements BeerControllerDocs {

  private final BeerService beerService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public BeerDTO createBeer(@RequestBody @Valid BeerDTO beerDTO) throws BeerAlreadyRegisteredException {
    return beerService.createBeer(beerDTO);
  }

  @GetMapping("/{name}")
  public BeerDTO findByName(@PathVariable String name) throws BeerNotFoundException {
    return beerService.findByName(name);
  }

  @GetMapping
  public List<BeerDTO> listBeers() {
    return beerService.listAll();
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteById(@PathVariable Long id) throws BeerNotFoundException {
    beerService.deleteById(id);
  }

  @PatchMapping("/{id}/increment")
  public BeerDTO increment(@PathVariable Long id, @Valid @RequestBody QuantityDTO quantityDTO)
      throws BeerNotFoundException, BeerStockExceededException {
    return beerService.increment(id, quantityDTO.getQuantity());
  }

  @PatchMapping("/{id}/decrement")
  public BeerDTO decrement(
      @PathVariable(name = "id") Long beerId,
      @Valid @RequestBody QuantityDTO quantityToDecrementDTO)
      throws BeerNotFoundException, BeerStockExceededException {
    return beerService.decrement(beerId, quantityToDecrementDTO.getQuantity());
  }
}
