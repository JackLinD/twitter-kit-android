package com.twitter.sdk.android.core.services;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.models.Tweet;

import java.util.List;

import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;

public interface StatusesService {

    /**
     * Returns most recent mentions (tweets containing a user's @screen_name) for the
     * authenticating user, by default returns 20 tweets.
     * <p>
     * The timeline returned is the equivalent of the one seen when you view your mentions on
     * twitter.com.
     * <p>
     * The Twitter REST API goes back up to 800 tweets.
     *
     * @param count (optional) Specifies the number of tweets to try and retrieve, up to a maximum
     *              of 200. The value of count is best thought of as a limit to the number of tweets
     *              to return because suspended or deleted content is removed after the count has
     *              been applied. We include retweets in the count, even if include_rts is not
     *              supplied. It is recommended you always send include_rts=1 when using this API
     *              method.
     * @param sinceId (optional) Returns results with an ID greater than (that is, more recent than)
     *                the specified ID. There are limits to the number of Tweets which can be
     *                accessed through the API. If the limit of Tweets has occurred since the
     *                since_id, the since_id will be forced to the oldest ID available.
     * @param maxId (optional) Returns results with an ID less than (that is, older than) or equal
     *              to the specified ID.
     * @param trimUser (optional) When set to either true, t or 1, each tweet returned in a timeline
     *                 will include a user object including only the status authors numerical ID.
     *                 Omit this parameter to receive the complete user object.
     * @param contributeDetails (optional) This parameter enhances the contributors element of the
     *                          status response to include the screen_name of the contributor. By
     *                          default only the user_id of the contributor is included.
     * @param includeEntities (optional) The entities node will be disincluded when set to false.
     * @param cb The callback to invoke when the request completes.
     */
    @GET("/1.1/statuses/mentions_timeline.json")
    void mentionsTimeline(@Query("count") Integer count,
                          @Query("since_id") Long sinceId,
                          @Query("max_id") Long maxId,
                          @Query("trim_user") Boolean trimUser,
                          @Query("contributor_details") Boolean contributeDetails,
                          @Query("include_entities") Boolean includeEntities,
                          Callback<List<Tweet>> cb);

    /**
     * Returns a collection of the most recent Tweets posted by the user indicated by the
     * screen_name or user_id parameters.
     * <p>
     * User timelines belonging to protected users may only be requested when the authenticated user
     * either "owns" the timeline or is an approved follower of the owner.
     * <p>
     * The timeline returned is the equivalent of the one seen when you view a user's profile on
     * twitter.com.
     * <p>
     * The Twitter REST API goes back up to 3,200 of a user's most recent tweets.
     * Native retweets of other statuses by the user is included in this total, regardless of
     * whether include_rts is set to false when requesting this resource.
     * <p>
     * Always specify either an user_id or screen_name when requesting a user timeline.
     *
     * @param userId (optional) The ID of the user for whom to return results for.
     * @param screenName (optional) The screen name of the user for whom to return results for.
     * @param count (optional) Specifies the number of tweets to try and retrieve, up to a maximum
     *              of 200. The value of count is best thought of as a limit to the number of tweets
     *              to return because suspended or deleted content is removed after the count has
     *              been applied. We include retweets in the count, even if include_rts is not
     *              supplied. It is recommended you always send include_rts=1 when using this API
     *              method.
     * @param sinceId (optional) Returns results with an ID greater than (that is, more recent than)
     *                the specified ID. There are limits to the number of Tweets which can be
     *                accessed through the API. If the limit of Tweets has occurred since the
     *                since_id, the since_id will be forced to the oldest ID available.
     * @param maxId (optional) Returns results with an ID less than (that is, older than) or equal
     *              to the specified ID.
     * @param trimUser (optional) When set to either true, t or 1, each tweet returned in a timeline
     *                 will include a user object including only the status authors numerical ID.
     *                 Omit this parameter to receive the complete user object.
     * @param excludeReplies (optional) This parameter will prevent replies from appearing in the
     *                       returned timeline. Using exclude_replies with the count parameter will
     *                       mean you will receive up-to count tweets — this is because the count
     *                       parameter retrieves that many tweets before filtering out retweets and
     *                       replies. This parameter is only supported for JSON and XML responses.
     * @param contributeDetails (optional) This parameter enhances the contributors element of the
     *                          status response to include the screen_name of the contributor. By
     *                          default only the user_id of the contributor is included.
     * @param includeRetweets (optional) When set to false, the timeline will strip any native
     *                        retweets (though they will still count toward both the maximal length
     *                        of the timeline and the slice selected by the count parameter).
     *                        Note: If you're using the trim_user parameter in conjunction with
     *                        include_rts, the retweets will still contain a full user object.
     * @param cb The callback to invoke when the request completes.
     */
    @GET("/1.1/statuses/user_timeline.json")
    void userTimeline(@Query("user_id") Long userId,
                      @Query("screen_name") String screenName,
                      @Query("count") Integer count,
                      @Query("since_id") Long sinceId,
                      @Query("max_id") Long maxId,
                      @Query("trim_user") Boolean trimUser,
                      @Query("exclude_replies") Boolean excludeReplies,
                      @Query("contributor_details") Boolean contributeDetails,
                      @Query("include_rts") Boolean includeRetweets,
                      Callback<List<Tweet>> cb);

    /**
     * Returns a collection of the most recent Tweets and retweets posted by the authenticating user
     * and the users they follow. The home timeline is central to how most users interact with the
     * Twitter service.
     * <p>
     * The Twitter REST API goes back up to 800 tweets on the home timeline.
     * It is more volatile for users that follow many users or follow users who tweet frequently.
     *
     * @param count (optional) Specifies the number of tweets to try and retrieve, up to a maximum
     *              of 200. The value of count is best thought of as a limit to the number of tweets
     *              to return because suspended or deleted content is removed after the count has
     *              been applied. We include retweets in the count, even if include_rts is not
     *              supplied. It is recommended you always send include_rts=1 when using this API
     *              method.
     * @param sinceId (optional) Returns results with an ID greater than (that is, more recent than)
     *                the specified ID. There are limits to the number of Tweets which can be
     *                accessed through the API. If the limit of Tweets has occurred since the
     *                since_id, the since_id will be forced to the oldest ID available.
     * @param maxId (optional) Returns results with an ID less than (that is, older than) or equal
     *              to the specified ID.
     * @param trimUser (optional) When set to either true, t or 1, each tweet returned in a timeline
     *                 will include a user object including only the status authors numerical ID.
     *                 Omit this parameter to receive the complete user object.
     * @param excludeReplies (optional) This parameter will prevent replies from appearing in the
     *                       returned timeline. Using exclude_replies with the count parameter will
     *                       mean you will receive up-to count tweets — this is because the count
     *                       parameter retrieves that many tweets before filtering out retweets and
     *                       replies. This parameter is only supported for JSON and XML responses.
     * @param contributeDetails (optional) This parameter enhances the contributors element of the
     *                          status response to include the screen_name of the contributor. By
     *                          default only the user_id of the contributor is included.
     * @param includeEntities (optional) The entities node will be disincluded when set to false.
     * @param cb The callback to invoke when the request completes.
     */
    @GET("/1.1/statuses/home_timeline.json")
    void homeTimeline(@Query("count") Integer count,
                      @Query("since_id") Long sinceId,
                      @Query("max_id") Long maxId,
                      @Query("trim_user") Boolean trimUser,
                      @Query("exclude_replies") Boolean excludeReplies,
                      @Query("contributor_details") Boolean contributeDetails,
                      @Query("include_entities") Boolean includeEntities,
                      Callback<List<Tweet>> cb);

    /**
     * Returns the most recent tweets authored by the authenticating user that have been retweeted
     * by others. This timeline is a subset of the user's GET statuses / user_timeline.
     *
     * @param count (optional) Specifies the number of tweets to try and retrieve, up to a maximum
     *              of 200. The value of count is best thought of as a limit to the number of tweets
     *              to return because suspended or deleted content is removed after the count has
     *              been applied. We include retweets in the count, even if include_rts is not
     *              supplied. It is recommended you always send include_rts=1 when using this API
     *              method.
     * @param sinceId (optional) Returns results with an ID greater than (that is, more recent than)
     *                the specified ID. There are limits to the number of Tweets which can be
     *                accessed through the API. If the limit of Tweets has occurred since the
     *                since_id, the since_id will be forced to the oldest ID available.
     * @param maxId (optional) Returns results with an ID less than (that is, older than) or equal
     *              to the specified ID.
     * @param trimUser (optional) When set to either true, t or 1, each tweet returned in a timeline
     *                 will include a user object including only the status authors numerical ID.
     *                 Omit this parameter to receive the complete user object.
     * @param includeEntities (optional) The entities node will be disincluded when set to false.
     * @param includeUserEntities (optional) The user entities node will not be included when set to
     *                            false.
     * @param cb The callback to invoke when the request completes.
     */
    @GET("/1.1/statuses/retweets_of_me.json")
    void retweetsOfMe(@Query("count") Integer count,
                      @Query("since_id") Long sinceId,
                      @Query("max_id") Long maxId,
                      @Query("trim_user") Boolean trimUser,
                      @Query("include_entities") Boolean includeEntities,
                      @Query("include_user_entities") Boolean includeUserEntities,
                      Callback<List<Tweet>> cb);

    /**
     * Returns a single Tweet, specified by the id parameter. The Tweet's author will also be
     * embedded within the tweet.
     *
     * @param id (required) The numerical ID of the desired Tweet.
     * @param trimUser (optional) When set to either true, t or 1, each tweet returned in a timeline
     *                 will include a user object including only the status authors numerical ID.
     *                 Omit this parameter to receive the complete user object.
     * @param includeMyRetweet (optional) When set to either true, t or 1, any Tweets returned that
     *                         have been retweeted by the authenticating user will include an
     *                         additional current_user_retweet node, containing the ID of the source
     *                         status for the retweet.
     * @param includeEntities (optional) The entities node will be disincluded when set to false.
     * @param cb The callback to invoke when the request completes.
     */
    @GET("/1.1/statuses/show.json")
    void show(@Query("id") Long id,
              @Query("trim_user") Boolean trimUser,
              @Query("include_my_retweet") Boolean includeMyRetweet,
              @Query("include_entities") Boolean includeEntities,
              Callback<Tweet> cb);

    /**
     * Returns fully-hydrated tweet objects for up to 100 tweets per request, as specified by
     * comma-separated values passed to the id parameter.
     * <p>
     * This method is especially useful to get the details (hydrate) a collection of Tweet IDs.
     * <p>
     * GET statuses / show / :id is used to retrieve a single tweet object.
     * <p>
     * There are a few things to note when using this method.
     * <li>You must be following a protected user to be able to see their most recent tweets. If you
     * don't follow a protected user their status will be removed.</li>
     * <li>The order of tweet IDs may not match the order of tweets in the returned array.</li>
     * <li>If a requested tweet is unknown or deleted, then that tweet will not be returned in the
     * results list, unless the map parameter is set to true, in which case it will be returned with
     * a value of null.</li>
     * <li>If none of your lookup criteria matches valid tweet IDs an empty array will be returned
     * for map=false.</li>
     * <li>You are strongly encouraged to use a POST for larger requests.</li>
     *
     * @param id (required) The numerical ID of the desired Tweet.
     * @param includeEntities (optional) The entities node will be disincluded when set to false.
     * @param trimUser (optional) When set to either true, t or 1, each tweet returned in a timeline
     *                 will include a user object including only the status authors numerical ID.
     *                 Omit this parameter to receive the complete user object.
     * @param map (optional) When using the map parameter, tweets that do not exist or cannot be
     *            viewed by the current user will still have their key represented but with an
     *            explicitly null value paired with it
     * @param cb The callback to invoke when the request completes.
     */
    @GET("/1.1/statuses/lookup.json")
    void lookup(@Query("id") String id,
                @Query("include_entities") Boolean includeEntities,
                @Query("trim_user") Boolean trimUser,
                @Query("map") Boolean map,
                Callback<List<Tweet>> cb);

    /**
     * Updates the authenticating user's current status, also known as tweeting.
     * <p>
     * For each update attempt, the update text is compared with the authenticating user's recent
     * tweets. Any attempt that would result in duplication will be blocked, resulting in a 403
     * error. Therefore, a user cannot submit the same status twice in a row.
     * <p>
     * While not rate limited by the API a user is limited in the number of tweets they can create
     * at a time. If the number of updates posted by the user reaches the current allowed limit this
     * method will return an HTTP 403 error.
     *
     * @param status (required) The text of your status update, typically up to 140 characters. URL
     *               encode as necessary. [node:840,title="t.co link wrapping"] may effect character
     *               counts. There are some special commands in this field to be aware of. For
     *               instance, preceding a message with "D " or "M " and following it with a screen
     *               name can create a direct message to that user if the relationship allows for
     *               it.
     * @param inReplyToStatusId (optional) The ID of an existing status that the update is in reply
     *                          to. Note:: This parameter will be ignored unless the author of the
     *                          tweet this parameter references is mentioned within the status text.
     *                          Therefore, you must include @username, where username is the author
     *                          of the referenced tweet, within the update.
     * @param possiblySensitive (optional) If you upload Tweet media that might be considered
     *                          sensitive content such as nudity, violence, or medical procedures,
     *                          you should set this value to true. See Media setting and best
     *                          practices for more context. Defaults to false.
     * @param latitude (optional) The latitude of the location this tweet refers to. This parameter
     *                 will be ignored unless it is inside the range -90.0 to +90.0 (North is
     *                 positive) inclusive. It will also be ignored if there isn't a corresponding
     *                 long parameter.
     * @param longitude (optional) The longitude of the location this tweet refers to. The valid
     *                  ranges for longitude is -180.0 to +180.0 (East is positive) inclusive. This
     *                  parameter will be ignored if outside that range, if it is not a number, if
     *                  geo_enabled is disabled, or if there not a corresponding lat parameter.
     * @param placeId (optional) A place in the world. These IDs can be retrieved from [node:29].
     * @param displayCoordinates (optional) Whether or not to put a pin on the exact coordinates a
     *                           tweet has been sent from.
     * @param trimUser (optional) When set to either true, t or 1, each tweet returned in a timeline
     *                 will include a user object including only the status authors numerical ID.
     *                 Omit this parameter to receive the complete user object.
     * @param cb The callback to invoke when the request completes.
     */
    @FormUrlEncoded
    @POST("/1.1/statuses/update.json")
    void update(@Field("status") String status,
           @Field("in_reply_to_status_id") Long inReplyToStatusId,
           @Field("possibly_sensitive") Boolean possiblySensitive,
           @Field("lat") Double latitude,
           @Field("long") Double longitude,
           @Field("place_id") String placeId,
           @Field("display_cooridnates") Boolean displayCoordinates,
           @Field("trim_user") Boolean trimUser,
           Callback<Tweet> cb);

    /**
     * Returns a collection of the 100 most recent retweets of the tweet specified by the id
     * parameter.
     *
     * @param id (required) The numerical ID of the desired Tweet.
     * @param trimUser (optional) When set to either true, t or 1, each tweet returned in a timeline
     *                 will include a user object including only the status authors numerical ID.
     *                 Omit this parameter to receive the complete user object.
     * @param cb The callback to invoke when the request completes.
     */
    @FormUrlEncoded
    @POST("/1.1/statuses/retweet/{id}.json")
    void retweet(@Path("id") Long id,
                 @Field("trim_user") Boolean trimUser,
                 Callback<Tweet> cb);

    /**
     * Destroys the status specified by the required ID parameter. The authenticating user must be
     * the author of the specified status. Returns the destroyed status if successful.
     *
     * @param id (required) The numerical ID of the desired Tweet.
     * @param trimUser (optional) When set to either true, t or 1, each tweet returned in a timeline
     *                 will include a user object including only the status authors numerical ID.
     *                 Omit this parameter to receive the complete user object.
     * @param cb The callback to invoke when the request completes.
     */
    @FormUrlEncoded
    @POST("/1.1/statuses/destroy/{id}.json")
    void destroy(@Path("id") Long id,
                 @Field("trim_user") Boolean trimUser,
                 Callback<Tweet> cb);
}
