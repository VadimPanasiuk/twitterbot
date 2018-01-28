package com.vadim.twitterbot.controller;

import com.vadim.twitterbot.entity.TweetContent;
import org.springframework.social.OperationNotPermittedException;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.twitter.api.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.inject.Inject;
import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/")
public class HelloController {

    private Twitter twitter;

    private ConnectionRepository connectionRepository;

    @Inject
    public HelloController(Twitter twitter, ConnectionRepository connectionRepository) {
        this.twitter = twitter;
        this.connectionRepository = connectionRepository;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String helloTwitter(Model model) {
        if (connectionRepository.findPrimaryConnection(Twitter.class) == null) {
            return "redirect:/connect/twitter";
        }

        model.addAttribute(twitter.userOperations().getUserProfile());
        model.addAttribute("tweetContent", new TweetContent());
        return "tweet";
    }

    @RequestMapping(value = "/retweet", method = RequestMethod.POST)
    public String searchRetweetTweet(@ModelAttribute(value = "tweetContent") TweetContent tweetContent, Model model) {
        if (connectionRepository.findPrimaryConnection(Twitter.class) == null) {
            return "redirect:/connect/twitter";
        }
        model.addAttribute(twitter.userOperations().getUserProfile());

        // List of popular tweet
        List<Tweet> tweets = searchTweets(twitter, tweetContent.getSearchParameter());

        // The person with the most retweeted tweet for that day
        Tweet maxRetweetTweet = tweets.stream().max(Comparator.comparing(Tweet::getRetweetCount)).get();
        TwitterProfile maxRetweetTweetUser = getMaxRetweetTweetUser(tweets);
        model.addAttribute("toptweeted", maxRetweetTweetUser.getScreenName());

        // Follow to this user
        String follow = twitter.friendOperations().follow(maxRetweetTweetUser.getId());
        if (follow.equals(maxRetweetTweetUser.getScreenName())) {
            model.addAttribute("follow", follow);
        } else {
            model.addAttribute("follow", "Following error");
        }

        // Retweet this tweet
        Tweet retweet;
        try {
            retweet = twitter.timelineOperations().retweet(maxRetweetTweet.getId());

        } catch (OperationNotPermittedException e) {
            model.addAttribute("retweet", e.getMessage());
            return "tweet";
        }
        model.addAttribute("retweet", retweet.getText());
        return "tweet";
    }

    private TwitterProfile getMaxRetweetTweetUser(List<Tweet> tweets) {
        Tweet maxRetweetTweet = tweets.stream().max(Comparator.comparing(Tweet::getRetweetCount)).get();
        return maxRetweetTweet.getUser();
    }

    private List<Tweet> searchTweets(Twitter twitter, String searchWord) {
        SearchResults results = twitter.searchOperations().search(new SearchParameters(searchWord)
                .resultType(SearchParameters.ResultType.POPULAR)
                .count(25)
                .includeEntities(false));

        return results.getTweets();
    }

}
