package me.dio.rodolfohok.beerstock.controller;

import me.dio.rodolfohok.beerstock.builder.BeerDTOBuilder;
import me.dio.rodolfohok.beerstock.dto.BeerDTO;
import me.dio.rodolfohok.beerstock.dto.QuantityDTO;
import me.dio.rodolfohok.beerstock.exception.BeerNotFoundException;
import me.dio.rodolfohok.beerstock.exception.BeerStockExceededException;
import me.dio.rodolfohok.beerstock.service.BeerService;
import me.dio.rodolfohok.beerstock.utils.JsonConversionUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import java.util.Collections;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class BeerControllerTest {

  private static final String BEER_API_URL_PATH = "/api/v1/beers";
  private static final long VALID_BEER_ID = 1L;
  private static final long INVALID_BEER_ID = 2L;
  private static final String BEER_API_SUBPATH_INCREMENT_URL = "/increment";
  private static final String BEER_API_SUBPATH_DECREMENT_URL = "/decrement";

  @Mock
  private BeerService beerService;

  @InjectMocks
  private BeerController beerController;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(beerController)
        .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
        .setViewResolvers((viewName, locale) -> new MappingJackson2JsonView())
        .build();
  }

  @Test
  void whenPOSTIsCalledThenABeerIsCreated() throws Exception {
    // given
    BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
    // when
    when(beerService.createBeer(beerDTO)).thenReturn(beerDTO);
    // then
    mockMvc.perform(post(BEER_API_URL_PATH)
            .contentType(MediaType.APPLICATION_JSON)
            .content(JsonConversionUnit.asJsonString(beerDTO)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.name", is(beerDTO.getName())))
        .andExpect(jsonPath("$.brand", is(beerDTO.getBrand())))
        .andExpect(jsonPath("$.type", is(beerDTO.getType().toString())));
  }

  @Test
  void whenPOSTIsCalledWithoutRequiredFieldThenAnErrorReturned() throws Exception {
    // given
    BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
    beerDTO.setBrand(null);
    // then
    mockMvc.perform(post(BEER_API_URL_PATH)
            .contentType(MediaType.APPLICATION_JSON)
            .content(JsonConversionUnit.asJsonString(beerDTO)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void whenGETIsCalledWithAValidNameThenOkStatusIsReturned() throws Exception {
    // given
    BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
    // when
    when(beerService.findByName(beerDTO.getName())).thenReturn(beerDTO);
    // then
    mockMvc.perform(get(BEER_API_URL_PATH + "/" + beerDTO.getName())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name", is(beerDTO.getName())))
        .andExpect(jsonPath("$.brand", is(beerDTO.getBrand())))
        .andExpect(jsonPath("$.type", is(beerDTO.getType().toString())));
  }

  @Test
  void whenGETIsCalledWithoutRegisteredAValidNameThenNotFoundStatusIsReturned() throws Exception {
    // given
    BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
    // when
    when(beerService.findByName(beerDTO.getName())).thenThrow(BeerNotFoundException.class);
    // then
    mockMvc.perform(get(BEER_API_URL_PATH + "/" + beerDTO.getName())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  void whenGETListOfBeersIsCalledThenOkStatusIsReturned() throws Exception {
    // given
    BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
    // when
    when(beerService.listAll()).thenReturn(Collections.singletonList(beerDTO));
    // then
    mockMvc.perform(get(BEER_API_URL_PATH)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].name", is(beerDTO.getName())))
        .andExpect(jsonPath("$[0].brand", is(beerDTO.getBrand())))
        .andExpect(jsonPath("$[0].type", is(beerDTO.getType().toString())));
  }

  @Test
  void whenGETListWithoutBeersIsCalledThenOkStatusIsReturned() throws Exception {
    // when
    when(beerService.listAll()).thenReturn(Collections.emptyList());
    // then
    mockMvc.perform(get(BEER_API_URL_PATH)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  void whenDELETEIsCalledWithAValidIdThenNoContentStatusIsReturned() throws Exception {
    // given
    BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
    // when
    doNothing().when(beerService).deleteById(beerDTO.getId());
    // then
    mockMvc.perform(delete(BEER_API_URL_PATH + "/" + beerDTO.getId().toString())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());
  }

  @Test
  void whenDELETEIsCalledWithAInvalidIdThenNotFoundStatusIsReturned() throws Exception {
    // when
    doThrow(BeerNotFoundException.class).when(beerService).deleteById(INVALID_BEER_ID);
    // then
    mockMvc.perform(delete(BEER_API_URL_PATH + "/" + INVALID_BEER_ID)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  void whenPATCHIsCalledToIncrementDiscountThenOKstatusIsReturned() throws Exception {
    // given
    QuantityDTO quantityDTO = QuantityDTO.builder()
        .quantity(10)
        .build();
    BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
    beerDTO.setQuantity(beerDTO.getQuantity() + quantityDTO.getQuantity());
    // when
    when(beerService.increment(VALID_BEER_ID, quantityDTO.getQuantity())).thenReturn(beerDTO);
    // then
    mockMvc.perform(patch(BEER_API_URL_PATH + "/" + VALID_BEER_ID + BEER_API_SUBPATH_INCREMENT_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .content(JsonConversionUnit.asJsonString(quantityDTO))).andExpect(status().isOk())
        .andExpect(jsonPath("$.name", is(beerDTO.getName())))
        .andExpect(jsonPath("$.brand", is(beerDTO.getBrand())))
        .andExpect(jsonPath("$.type", is(beerDTO.getType().toString())))
        .andExpect(jsonPath("$.quantity", is(beerDTO.getQuantity())));
  }

  @Test
  void whenPATCHIsCalledToIncrementGreaterThanMaxThenBadRequestStatusIsReturned() throws Exception {
    // given
    QuantityDTO quantityDTO = QuantityDTO.builder()
        .quantity(30)
        .build();
    BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
    beerDTO.setQuantity(beerDTO.getQuantity() + quantityDTO.getQuantity());
    // when
    when(beerService.increment(VALID_BEER_ID, quantityDTO.getQuantity())).thenThrow(BeerStockExceededException.class);
    // then
    mockMvc.perform(patch(BEER_API_URL_PATH + "/" + VALID_BEER_ID + BEER_API_SUBPATH_INCREMENT_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .content(JsonConversionUnit.asJsonString(quantityDTO)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void whenPATCHIsCalledWithInvalidBeerIdToIncrementThenNotFoundStatusIsReturned() throws Exception {
    // given
    QuantityDTO quantityDTO = QuantityDTO.builder()
        .quantity(30)
        .build();
    // when
    when(beerService.increment(INVALID_BEER_ID, quantityDTO.getQuantity())).thenThrow(BeerNotFoundException.class);
    // then
    mockMvc.perform(patch(BEER_API_URL_PATH + "/" + INVALID_BEER_ID + BEER_API_SUBPATH_INCREMENT_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .content(JsonConversionUnit.asJsonString(quantityDTO)))
        .andExpect(status().isNotFound());
  }

  @Test
  void whenPATCHIsCalledToDecrementDiscountThenOkStatusIsReturned() throws Exception {
    // given
    QuantityDTO quantityDTO = QuantityDTO.builder()
        .quantity(5)
        .build();
    BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
    beerDTO.setQuantity(beerDTO.getQuantity() + quantityDTO.getQuantity());
    // when
    when(beerService.decrement(VALID_BEER_ID, quantityDTO.getQuantity())).thenReturn(beerDTO);
    // then
    mockMvc.perform(patch(BEER_API_URL_PATH + "/" + VALID_BEER_ID + BEER_API_SUBPATH_DECREMENT_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .content(JsonConversionUnit.asJsonString(quantityDTO))).andExpect(status().isOk())
        .andExpect(jsonPath("$.name", is(beerDTO.getName())))
        .andExpect(jsonPath("$.brand", is(beerDTO.getBrand())))
        .andExpect(jsonPath("$.type", is(beerDTO.getType().toString())))
        .andExpect(jsonPath("$.quantity", is(beerDTO.getQuantity())));
  }

  @Test
  void whenPATCHIsCalledToDecrementLowerThanZeroThenBadRequestStatusIsReturned() throws Exception {
    // given
    QuantityDTO quantityDTO = QuantityDTO.builder()
        .quantity(60)
        .build();
    BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
    beerDTO.setQuantity(beerDTO.getQuantity() + quantityDTO.getQuantity());
    // when
    when(beerService.decrement(VALID_BEER_ID, quantityDTO.getQuantity())).thenThrow(BeerStockExceededException.class);
    // then
    mockMvc.perform(patch(BEER_API_URL_PATH + "/" + VALID_BEER_ID + BEER_API_SUBPATH_DECREMENT_URL)
        .contentType(MediaType.APPLICATION_JSON)
        .content(JsonConversionUnit.asJsonString(quantityDTO))).andExpect(status().isBadRequest());
  }

  @Test
  void whenPATCHIsCalledWithInvalidBeerIdToDecrementThenNotFoundStatusIsReturned() throws Exception {
    // given
    QuantityDTO quantityDTO = QuantityDTO.builder()
        .quantity(5)
        .build();
    // when
    when(beerService.decrement(INVALID_BEER_ID, quantityDTO.getQuantity())).thenThrow(BeerNotFoundException.class);
    // then
    mockMvc.perform(patch(BEER_API_URL_PATH + "/" + INVALID_BEER_ID + BEER_API_SUBPATH_DECREMENT_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .content(JsonConversionUnit.asJsonString(quantityDTO)))
        .andExpect(status().isNotFound());
  }
}
