package com.handlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.zc.api.APIConstants.RequestMethod;
import com.zc.api.APIRequest;
import com.zc.cliq.enums.SLIDE_TYPE;
import com.zc.cliq.objects.BotDetails;
import com.zc.cliq.objects.CommandSuggestion;
import com.zc.cliq.objects.MessageBuilder;
import com.zc.cliq.objects.Slide;
import com.zc.cliq.requests.CommandHandlerRequest;
import com.zc.cliq.util.ZCCliqUtil;

import okhttp3.Response;

public class CommandHandler implements com.zc.cliq.interfaces.CommandHandler
{
	@Override
	public Map<String, Object> executionHandler(CommandHandlerRequest req) throws Exception{
		
		MessageBuilder msg = MessageBuilder.getInstance();
		
		BotDetails bot = BotDetails.getInstance(GithubConstants.BOT_NAME);
		msg.setBot(bot);
		String commandName = req.getName();
		
		if(commandName.equals("commits")){
			List<CommandSuggestion> repoSuggestions = req.getSelections();
			if(repoSuggestions == null || repoSuggestions.isEmpty()){
				msg.setText("Please select a repo from the suggestions.");
			}else{
				String repoName = repoSuggestions.get(0).getTitle();
				JSONArray commitsArray = getCommits(repoName);
				if(commitsArray.length() == 0){
					msg.setText("There aren't are commits made yet.");
				}
				else{
					Slide slide = Slide.getInstance();
					msg.setText("Here's a list of the latest " + GithubConstants.PER_PAGE + " commits made to the repository *" + repoName + "*.");
					slide.setType(SLIDE_TYPE.TABLE);
					slide.setTitle("Commit details");
					List<String> headers = new ArrayList<String>();
					headers.add("Date");
					headers.add("Commit message");
					headers.add("Committed by");
					headers.add("Link");
					JSONObject data = new JSONObject();
					data.put("headers", headers);
					JSONArray rows = new JSONArray();
					
					for(int i=0; i<commitsArray.length(); i++){
						JSONObject obj = commitsArray.optJSONObject(i);
						JSONObject commit = obj.optJSONObject("commit");
						JSONObject author = commit.optJSONObject("author");
						
						JSONObject row = new JSONObject();
						row.put("Date", author.optString("date"));
						row.put("Commit message", commit.optString("message"));
						row.put("Committed by", author.optString("name"));
						row.put("Link", "[Click here](" + obj.optString("html_url") + ")");
						
						rows.put(row);
					}
					data.put("rows", rows);
					slide.setData(data);
					msg.addSlide(slide);
				}
				
			}
		}
		else if(commandName.equals("issues")){
			List<CommandSuggestion> repoSuggestions = req.getSelections();
			if(repoSuggestions == null || repoSuggestions.isEmpty()){
				msg.setText("Please select a repo from the suggestions.");
			}else{
				String repoName = repoSuggestions.get(0).getTitle();
				JSONArray issuesArray = getIssues(repoName);
				if(issuesArray.length() == 0){
					msg.setText("There aren't are issues raised yet.");
				}
				else{
					Slide slide = Slide.getInstance();
					msg.setText("Here's a list of the latest " + GithubConstants.PER_PAGE + " issues raised to the repository *" + repoName + "*");
					slide.setType(SLIDE_TYPE.TABLE);
					slide.setTitle("Issue details");
					List<String> headers = new ArrayList<String>();
					headers.add("Created At");
					headers.add("Title");
					headers.add("Created By");
					headers.add("Link");
					JSONObject data = new JSONObject();
					data.put("headers", headers);
					JSONArray rows = new JSONArray();
					
					for(int i=0; i<issuesArray.length(); i++){
						JSONObject issueObj = issuesArray.optJSONObject(i);
						
						JSONObject row = new JSONObject();
						row.put("Created At", issueObj.optString("created_at"));
						row.put("Title", issueObj.optString("title"));
						row.put("Created By", issueObj.optJSONObject("user").optString("login"));
						row.put("Link", "[Click here](" + issueObj.optString("html_url") + ")");
						
						rows.put(row);
					}
					data.put("rows", rows);
					slide.setData(data);
					msg.addSlide(slide);
				}
				
			}
		}
		else{
			msg.setText("Slash command executed");
		}
		
		return ZCCliqUtil.toMap(msg);
	}

	@Override
	public List<CommandSuggestion> suggestionHandler(CommandHandlerRequest req) throws Exception {
		List<CommandSuggestion> suggestionList = new ArrayList<CommandSuggestion>();
		JSONArray reposArray = getRepos();
		List<String> repoNames = new ArrayList<>();
		for(int i=0; i< reposArray.length(); i++){
			JSONObject repo = reposArray.optJSONObject(i);
			repoNames.add(repo.optString("name"));
		}
		if(req.getName().equals("commits") || req.getName().equals("issues")){
			repoNames.forEach((name) -> {
				CommandSuggestion sugg = CommandSuggestion.getInstance();
				sugg.setTitle(name);
				suggestionList.add(sugg);
			});
		}
		return suggestionList;
	}

	public static JSONArray getRepos() throws Exception{
		APIRequest req = getRequestObj("https://api.github.com/user/repos");
		req.executeRequest();
		Response resp = req.getHttpResponse();
		JSONArray reposArray = new JSONArray(resp.body().string());
		
		return reposArray;
	}
	
	private String getUsername() throws Exception{
		APIRequest req = getRequestObj("https://api.github.com/user");
		JSONObject respJson = new JSONObject(req.getResponse().getResponseJSON().get(0).toString());
		return respJson.get("login").toString();
	}
	
	private JSONArray getCommits(String repoName) throws Exception{
		APIRequest req = getRequestObj("https://api.github.com/repos/" + getUsername() + "/" + repoName + "/commits?per_page=" + GithubConstants.PER_PAGE);
		req.executeRequest();
		Response resp = req.getHttpResponse();
		JSONArray commitsArray = new JSONArray(resp.body().string());
		return commitsArray;
	}
	
	private JSONArray getIssues(String repoName) throws Exception{
		APIRequest req = getRequestObj("https://api.github.com/repos/" + getUsername() + "/" + repoName + "/issues?per_page=" + GithubConstants.PER_PAGE);
		req.executeRequest();
		Response resp = req.getHttpResponse();
		JSONArray issuesArray = new JSONArray(resp.body().string());
		return issuesArray;
	}
	
	private static APIRequest getRequestObj(String url)
	{
		APIRequest req = new APIRequest();
		req.setUrl(url);
		req.setRequestMethod(RequestMethod.GET);
		req.setAuthNeeded(false);
		HashMap<String, String> headers = new HashMap<>();
		headers.put("Authorization", "Token " + GithubConstants.PERSONAL_ACCESS_TOKEN);
		req.setHeaders(headers);
		return req;
	}
}