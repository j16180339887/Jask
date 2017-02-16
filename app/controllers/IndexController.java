package controllers;

import play.mvc.*;
import views.html.*;

public class IndexController extends Controller {

    public Result index() {
        return ok(IndexPage.render());
    }
}
