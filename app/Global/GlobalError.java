package Global;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import play.http.HttpErrorHandler;
import play.mvc.Http.RequestHeader;
import play.mvc.Result;
import play.mvc.Results;
import views.html.*;

public class GlobalError implements HttpErrorHandler {

    @Override
    public CompletionStage<Result> onClientError(RequestHeader request, int statusCode, String message) {
//        if(statusCode == play.mvc.Http.Status.NOT_FOUND) {
//            // move your implementation of `GlobalSettings.onHandlerNotFound` here
//        	return CompletableFuture.completedFuture(
//                Results.notFound(index.render())
//            );
//        }

    	System.out.println(statusCode + " A client error occurred: " + message + "Request: " + request);
//      Results.status(statusCode, "A client error occurred: " + message)
        return CompletableFuture.completedFuture(
                Results.status( statusCode, ErrorPage.render("伺服器出錯了 :(" + message) )
        );
    }

    @Override
    public CompletionStage<Result> onServerError(RequestHeader request, Throwable exception) {
        return CompletableFuture.completedFuture(
                Results.internalServerError("A server error occurred: " + exception.getMessage())
        );
    }
}
