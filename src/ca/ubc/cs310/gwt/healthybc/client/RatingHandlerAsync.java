package ca.ubc.cs310.gwt.healthybc.client;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface RatingHandlerAsync {
	
	public void getRating(String refID, AsyncCallback<ArrayList<Integer>> callback);

	public void setRating(String refID, int score, AsyncCallback<ArrayList<Boolean>> callback);

	public void addReview(String refID, String review, AsyncCallback<ArrayList<String>> callback);

}