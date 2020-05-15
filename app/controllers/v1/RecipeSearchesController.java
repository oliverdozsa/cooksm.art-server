package controllers.v1;

import com.typesafe.config.Config;
import models.repositories.RecipeSearchRepository;
import play.Logger;
import play.data.FormFactory;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;

import javax.inject.Inject;

public class RecipeSearchesController extends Controller {
    @Inject
    private RecipeSearchRepository repository;

    @Inject
    private FormFactory formFactory;

    @Inject
    private HttpExecutionContext httpExecutionContext;

    @Inject
    private Config config;


    private static final Logger.ALogger logger = Logger.of(RecipeSearchesController.class);

    // TODO
}
