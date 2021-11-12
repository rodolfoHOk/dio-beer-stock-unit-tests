package me.dio.rodolfohok.beerstock.service;

import me.dio.rodolfohok.beerstock.builder.BeerDTOBuilder;
import me.dio.rodolfohok.beerstock.dto.BeerDTO;
import me.dio.rodolfohok.beerstock.entity.Beer;
import me.dio.rodolfohok.beerstock.exception.BeerAlreadyRegisteredException;
import me.dio.rodolfohok.beerstock.exception.BeerNotFoundException;
import me.dio.rodolfohok.beerstock.exception.BeerStockExceededException;
import me.dio.rodolfohok.beerstock.mapper.BeerMapper;
import me.dio.rodolfohok.beerstock.repository.BeerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BeerServiceTest {

  private static final long INVALID_BEER_ID = 2L;

  @Mock
  private BeerRepository beerRepository;

  @InjectMocks
  private BeerService beerService;

  private final BeerMapper beerMapper = BeerMapper.INSTANCE;

  @Test
  void whenBeerInformedThenItShouldBeCreated() throws BeerAlreadyRegisteredException {
    // given
    BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
    Beer expectedSavedBeer = beerMapper.toModel(expectedBeerDTO);

    // when
    when(beerRepository.findByName(expectedBeerDTO.getName())).thenReturn(Optional.empty());
    when(beerRepository.save(expectedSavedBeer)).thenReturn(expectedSavedBeer);

    // then
    BeerDTO createdBeerDTO = beerService.createBeer(expectedBeerDTO);

    // org.hamcrest.MatcherAssert.assertThat and org.hamcrest.Matchers.is and org.hamcrest.Matchers.equalTo
    // MatcherAssert.assertThat(createdBeerDTO.getId(), Matchers.is(Matchers.equalTo(expectedBeerDTO.getId())));
    // MatcherAssert.assertThat(createdBeerDTO.getId(), Matchers.is(Matchers.equalTo(expectedBeerDTO.getId())));
    assertThat(createdBeerDTO.getId(), is(equalTo(expectedBeerDTO.getId())));
    assertThat(createdBeerDTO.getName(), is(equalTo(expectedBeerDTO.getName())));
    assertThat(createdBeerDTO.getQuantity(), is(equalTo(expectedBeerDTO.getQuantity())));

    // assertThat(createdBeerDTO.getQuantity(), is(greaterThan(2))); ok

    // org.junit.jupiter.api.Assertions.assertEquals
    // Assertions.assertEquals(expectedBeerDTO.getId(), createdBeerDTO.getId());
    // Assertions.assertEquals(expectedBeerDTO.getName(), createdBeerDTO.getName());
    // assertEquals(expectedBeerDTO.getId(), createdBeerDTO.getId());
    // assertEquals(expectedBeerDTO.getName(), createdBeerDTO.getName());
  }

  @Test
  void whenAlreadyRegisteredBeerInformedThenAnExceptionShouldBeThrown() throws BeerAlreadyRegisteredException {
    // given
    BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
    Beer duplicatedBeer = beerMapper.toModel(expectedBeerDTO);
    // when
    when(beerRepository.findByName(expectedBeerDTO.getName())).thenReturn(Optional.of(duplicatedBeer));
    // then
    // Assertions.assertThrows(BeerAlreadyRegisteredException.class, () -> beerService.createBeer(expectedBeerDTO));
    assertThrows(BeerAlreadyRegisteredException.class, () -> beerService.createBeer(expectedBeerDTO));
  }

  @Test
  void whenValidNameBeerIsGivenThenReturnABeer() throws BeerNotFoundException {
    // given
    BeerDTO expectedFoundBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
    Beer expectedFoundBeer = beerMapper.toModel(expectedFoundBeerDTO);
    // when
    when(beerRepository.findByName(expectedFoundBeer.getName())).thenReturn(Optional.of(expectedFoundBeer));
    // then
    BeerDTO foundBeerDTO = beerService.findByName(expectedFoundBeerDTO.getName());
    assertThat(foundBeerDTO, is(equalTo(expectedFoundBeerDTO)));
  }

  @Test
  void whenNoRegisteredNameBeerIsGivenThenThrownAnException() {
    // given
    BeerDTO expectedFoundBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
    // when
    when(beerRepository.findByName(expectedFoundBeerDTO.getName())).thenReturn(Optional.empty());
    // then
    assertThrows(BeerNotFoundException.class, () -> beerService.findByName(expectedFoundBeerDTO.getName()));
  }

  @Test
  void whenListAllBeersIsCalledThenReturnAListOfBeers() {
    // given
    BeerDTO expectedFoundBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
    Beer expectedFoundBeer = beerMapper.toModel(expectedFoundBeerDTO);
    // when
    when(beerRepository.findAll()).thenReturn(Collections.singletonList(expectedFoundBeer));
    // then
    List<BeerDTO> foundBeersDTO = beerService.listAll();
    assertThat(foundBeersDTO, is(not(empty())));
    assertThat(foundBeersDTO.get(0), is(equalTo(expectedFoundBeerDTO)));
  }

  @Test
  void whenListAllBeersIsCalledThenReturnAnEmptyList() {
    // when
    when(beerRepository.findAll()).thenReturn(Collections.emptyList());
    // then
    List<BeerDTO> foundBeersDTO = beerService.listAll();
    assertThat(foundBeersDTO, is(empty()));
  }

  @Test
  void whenExclusionIsCalledWithAValidIdThenABeerShouldBeDeleted() throws BeerNotFoundException {
    // given
    BeerDTO expectedDeletedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
    Beer expectedDeletedBeer = beerMapper.toModel(expectedDeletedBeerDTO);
    // when
    when(beerRepository.findById(expectedDeletedBeerDTO.getId())).thenReturn(Optional.of(expectedDeletedBeer));
    doNothing().when(beerRepository).deleteById(expectedDeletedBeerDTO.getId());
    // then
    beerService.deleteById(expectedDeletedBeerDTO.getId());
    verify(beerRepository, times(1)).findById(expectedDeletedBeerDTO.getId());
    verify(beerRepository, times(1)).deleteById(expectedDeletedBeerDTO.getId());
  }

  @Test
  void whenExclusionIsCalledWithAInvalidIdThenThrowAnException() {
    // given
    BeerDTO expectedDeletedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
    // when
    when(beerRepository.findById(expectedDeletedBeerDTO.getId())).thenReturn(Optional.empty());
    // then
    assertThrows(BeerNotFoundException.class, () -> beerService.deleteById(expectedDeletedBeerDTO.getId()));
  }

  @Test
  void whenIncrementIsCalledThenIncrementBeerStock() throws BeerNotFoundException, BeerStockExceededException {
    // given
    BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
    Beer expectedBeer = beerMapper.toModel(expectedBeerDTO);
    // when
    when(beerRepository.findById(expectedBeerDTO.getId())).thenReturn(Optional.of(expectedBeer));
    when(beerRepository.save(expectedBeer)).thenReturn(expectedBeer);
    int quantityToIncrement = 10;
    int expectedQuantityAfterIncrement = expectedBeerDTO.getQuantity() + quantityToIncrement;
    // then
    BeerDTO incrementedBeerDTO = beerService.increment(expectedBeerDTO.getId(), quantityToIncrement);
    assertThat(expectedQuantityAfterIncrement, equalTo(incrementedBeerDTO.getQuantity()));
    assertThat(expectedQuantityAfterIncrement, lessThan(expectedBeerDTO.getMax()));
  }

  @Test
  void whenIncrementIsGreaterThanMaxThenThrowException() {
    // given
    BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
    Beer expectedBeer = beerMapper.toModel(expectedBeerDTO);
    // when
    when(beerRepository.findById(expectedBeerDTO.getId())).thenReturn(Optional.of(expectedBeer));
    int quantityToIncrement = 80;
    // then
    assertThrows(BeerStockExceededException.class, () -> beerService.increment(expectedBeerDTO.getId(), quantityToIncrement));
  }

  @Test
  void whenIncrementAfterSumIsGreaterThanMaxThenThrowException() {
    // given
    BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
    Beer expectedBeer = beerMapper.toModel(expectedBeerDTO);
    // when
    when(beerRepository.findById(expectedBeerDTO.getId())).thenReturn(Optional.of(expectedBeer));
    int quantityToIncrement = 45;
    // then
    assertThrows(BeerStockExceededException.class, () -> beerService.increment(expectedBeerDTO.getId(), quantityToIncrement));
  }

  @Test
  void whenIncrementIsCalledWithInvalidIdThenThrowException() {
    // given
    int quantityToIncrement = 10;
    // when
    when(beerRepository.findById(INVALID_BEER_ID)).thenReturn(Optional.empty());
    // then
    assertThrows(BeerNotFoundException.class, () -> beerService.increment(INVALID_BEER_ID, quantityToIncrement));
  }

  @Test
  void whenDecrementIsCalledThenDecrementBeerStock() throws BeerNotFoundException, BeerStockExceededException {
    // given
    BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
    Beer expectedBeer = beerMapper.toModel(expectedBeerDTO);
    // when
    when(beerRepository.findById(expectedBeerDTO.getId())).thenReturn(Optional.of(expectedBeer));
    when(beerRepository.save(expectedBeer)).thenReturn(expectedBeer);
    int quantityToDecrement = 5;
    int expectedQuantityAfterDecrement = expectedBeerDTO.getQuantity() - quantityToDecrement;
    // then
    BeerDTO incrementedBeerDTO = beerService.decrement(expectedBeerDTO.getId(), quantityToDecrement);
    assertThat(expectedQuantityAfterDecrement, equalTo(incrementedBeerDTO.getQuantity()));
    assertThat(expectedQuantityAfterDecrement, greaterThan(0));
  }

  @Test
  void whenDecrementIsCalledToEmptyStockThenEmptyBeerStock() throws BeerNotFoundException, BeerStockExceededException {
    // given
    BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
    Beer expectedBeer = beerMapper.toModel(expectedBeerDTO);
    // when
    when(beerRepository.findById(expectedBeerDTO.getId())).thenReturn(Optional.of(expectedBeer));
    when(beerRepository.save(expectedBeer)).thenReturn(expectedBeer);
    int quantityToDecrement = 10;
    int expectedQuantityAfterDecrement = expectedBeerDTO.getQuantity() - quantityToDecrement;
    // then
    BeerDTO incrementedBeerDTO = beerService.decrement(expectedBeerDTO.getId(), quantityToDecrement);
    assertThat(expectedQuantityAfterDecrement, equalTo(0));
    assertThat(expectedQuantityAfterDecrement, equalTo(incrementedBeerDTO.getQuantity()));
  }

  @Test
  void whenDecrementIsLowerThanZeroThenThrowException() {
    // given
    BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
    Beer expectedBeer = beerMapper.toModel(expectedBeerDTO);
    // when
    when(beerRepository.findById(expectedBeerDTO.getId())).thenReturn(Optional.of(expectedBeer));
    int quantityToDecrement = 80;
    // then
    assertThrows(BeerStockExceededException.class, () -> beerService.decrement(expectedBeerDTO.getId(), quantityToDecrement));
  }

//    @Test
//    void whenDecrementIsCalledWithInvalidIdThenThrowException() {
//        int quantityToDecrement = 10;
//
//        when(beerRepository.findById(INVALID_BEER_ID)).thenReturn(Optional.empty());
//
//        assertThrows(BeerNotFoundException.class, () -> beerService.decrement(INVALID_BEER_ID, quantityToDecrement));
//    }
}
