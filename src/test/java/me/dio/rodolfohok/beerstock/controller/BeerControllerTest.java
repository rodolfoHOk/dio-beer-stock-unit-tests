package me.dio.rodolfohok.beerstock.controller;

import me.dio.rodolfohok.beerstock.builder.BeerDTOBuilder;
import me.dio.rodolfohok.beerstock.dto.BeerDTO;
import me.dio.rodolfohok.beerstock.exception.BeerNotFoundException;
import me.dio.rodolfohok.beerstock.service.BeerService;
import me.dio.rodolfohok.beerstock.utils.JsonConversionUnit;
import org.hamcrest.Matchers;
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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
        .andExpect(jsonPath("$.name", Matchers.is(beerDTO.getName())))
        .andExpect(jsonPath("$.brand", Matchers.is(beerDTO.getBrand())))
        .andExpect(jsonPath("$.type", Matchers.is(beerDTO.getType().toString())));
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
        .andExpect(jsonPath("$.name", Matchers.is(beerDTO.getName())))
        .andExpect(jsonPath("$.brand", Matchers.is(beerDTO.getBrand())))
        .andExpect(jsonPath("$.type", Matchers.is(beerDTO.getType().toString())));
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
}
