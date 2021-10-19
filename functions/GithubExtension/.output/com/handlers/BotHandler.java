package com.handlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import com.zc.cliq.enums.ACTION_TYPE;
import com.zc.cliq.enums.BUTTON_TYPE;
import com.zc.cliq.enums.CHANNEL_OPERATION;
import com.zc.cliq.enums.SLIDE_TYPE;
import com.zc.cliq.objects.Action;
import com.zc.cliq.objects.ActionData;
import com.zc.cliq.objects.BotDetails;
import com.zc.cliq.objects.ButtonObject;
import com.zc.cliq.objects.Confirm;
import com.zc.cliq.objects.MessageBuilder;
import com.zc.cliq.objects.Slide;
import com.zc.cliq.requests.BotContextHandlerRequest;
import com.zc.cliq.requests.BotMentionHandlerRequest;
import com.zc.cliq.requests.BotMenuActionHandlerRequest;
import com.zc.cliq.requests.BotMessageHandlerRequest;
import com.zc.cliq.requests.BotParticipationHandlerRequest;
import com.zc.cliq.requests.BotWebhookHandlerRequest;
import com.zc.cliq.requests.BotWelcomeHandlerRequest;
import com.zc.cliq.util.ZCCliqUtil;

public class BotHandler implements com.zc.cliq.interfaces.BotHandler
{
	Logger LOGGER = Logger.getLogger(BotHandler.class.getName());
	
	@Override
	public Map<String,Object> messageHandler(BotMessageHandlerRequest req) throws JSONException {

		String message = req.getMessage();
		Map<String, Object> resp = new HashMap<String, Object>();
		
		String text;
		if(message == null){
			text = "Please enable 'Message' in bot settings";
		}
		else if(message.equalsIgnoreCase("webhooktoken")){
			
			MessageBuilder msg = MessageBuilder.getInstance("Click on the token generation button below!");
			ButtonObject btnObj = new ButtonObject();
			btnObj.setType(BUTTON_TYPE.GREEN_OUTLINE);
			btnObj.setLabel("Create Webhook");
			Action action = new Action();
			action.setType(ACTION_TYPE.INVOKE_FUNCTION);
			ActionData actionData = new ActionData();
			actionData.setName("authenticate"); // ** ENTER YOUR BUTTON FUNCTION NAME HERE **
			action.setData(actionData);
			Confirm confirm = new Confirm();
			confirm.setTitle("Generate Webhooks for a GitLab Project");
			confirm.setDescription("Connect to GitLab Projects from within Cliq");
			JSONObject input = new JSONObject();
			input.put("type", "user_webhook_token");
			confirm.setInput(input);
			action.setConfirm(confirm);
			btnObj.setAction(action);
			
			msg.addButton(btnObj);
			return ZCCliqUtil.toMap(msg);
		}
		else{
			text = "Sorry, I'm not programmed yet to do this :sad:";
		}
		
		resp.put("text", text);
		return resp;
	}
	
	@Override
	public Map<String, Object> menuActionHandler(BotMenuActionHandlerRequest req) throws Exception {
		MessageBuilder msg = MessageBuilder.getInstance();
		BotDetails bot = BotDetails.getInstance(GithubConstants.BOT_NAME);
		msg.setBot(bot);
		if(req.getActionName().equals("Repos")){
			JSONArray reposArray = CommandHandler.getRepos();
			if(reposArray.length() == 0){
				msg.setText("There aren't are repos created yet.");
			}
			else{
				Slide slide = Slide.getInstance();
				msg.setText("Here's a list of the *repositories*");
				slide.setType(SLIDE_TYPE.TABLE);
				slide.setTitle("Repo details");
				List<String> headers = new ArrayList<String>();
				headers.add("Name");
				headers.add("Private");
				headers.add("Open Issues");
				headers.add("Link");
				JSONObject data = new JSONObject();
				data.put("headers", headers);
				
				JSONArray rows = new JSONArray();
				for(int i=0; i< reposArray.length(); i++){
					JSONObject repo = reposArray.optJSONObject(i);
					JSONObject row = new JSONObject();
					row.put("Name", repo.optString("name"));
					row.put("Private", repo.optBoolean("private") ? "Yes" : "No");
					row.put("Open Issues", repo.optString("open_issues_count"));
					row.put("Link", "[Click here](" + repo.optString("html_url") + ")");
					
					rows.put(row);
				}
				data.put("rows", rows);
				slide.setData(data);
				msg.addSlide(slide);
			}
		}				
		else{
			msg.setText("Menu action triggered :fist:");
		}
		return ZCCliqUtil.toMap(msg);
	}

	@Override
	public Map<String, Object> webhookHandler(BotWebhookHandlerRequest req) throws Exception
	{	
		JSONObject reqBody = req.getBody();
		JSONObject commitJson = reqBody.optJSONArray("commits").optJSONObject(0);
		
		MessageBuilder msg = MessageBuilder.getInstance("A commit has been pushed !");
		msg.setBot(BotDetails.getInstance(GithubConstants.BOT_NAME));
		
		Slide commitMsg = Slide.getInstance();
		commitMsg.setType(SLIDE_TYPE.TEXT);
		commitMsg.setTitle("Commit message");
		commitMsg.setData(commitJson.optString("message"));
		msg.addSlide(commitMsg);
		
		Slide details = Slide.getInstance();
		details.setType(SLIDE_TYPE.LABEL);
		details.setTitle("Details");
		JSONArray dataArray = new JSONArray();
		JSONObject committer = new JSONObject();
		committer.put("Committer", commitJson.optJSONObject("author").optString("username"));
		dataArray.put(committer);
		JSONObject repoName = new JSONObject();
		repoName.put("Repo Name", reqBody.optJSONObject("repository").optString("name"));
		dataArray.put(repoName);
		JSONObject timestamp = new JSONObject();
		timestamp.put("Timestamp", commitJson.optString("timestamp"));
		dataArray.put(timestamp);
		JSONObject compare = new JSONObject();
		compare.put("Compare", "[Click here](" + reqBody.optString("compare") +  ")");
		dataArray.put(compare);
		details.setData(dataArray);
		msg.addSlide(details);
		
		return ZCCliqUtil.toMap(msg);
	}

	@Override
	public Map<String, Object> participationHandler(BotParticipationHandlerRequest req) throws Exception
	{
		String text;
		if(req.getOperation().equals(CHANNEL_OPERATION.ADDED)){
			text = "Hi. Thanks for adding me to the channel :smile:";
		}
		else if(req.getOperation().equals(CHANNEL_OPERATION.REMOVED)){
			text = "Bye-Bye :bye-bye:";
		}
		else{
			text = "I'm too a participant of this chat :wink:";
		}
		MessageBuilder msg = MessageBuilder.getInstance(text);
		return ZCCliqUtil.toMap(msg);
	}
	
	@Override
	public Map<String,Object> welcomeHandler(BotWelcomeHandlerRequest req) {
		String uName = req.getUser() != null ? req.getUser().getFirstName() : "user";
		String text = "Hello " + uName + ". Thank you for subscribing :smile:";
		MessageBuilder msg = MessageBuilder.getInstance(text);
		return ZCCliqUtil.toMap(msg);
	}
	
	@Override
	public Map<String, Object> contextHandler(BotContextHandlerRequest req) {
		
		Map<String, Object> resp = new HashMap<String, Object>();
		if(req.getContextId().equals("personal_details")){
			Map<String, String> answers = req.getAnswers();
			StringBuilder str = new StringBuilder();
			str.append("Name: ").append(answers.get("name")).append("\n");
			str.append("Department: ").append(answers.get("dept")).append("\n");
			
			resp.put("text", "Nice ! I have collected your info: \n" + str.toString());
		}
		return resp;
	}

	@Override
	public Map<String, Object> mentionHandler(BotMentionHandlerRequest req)
	{
		String text = "Hey *" + req.getUser().getFirstName() + "*, thanks for mentioning me here. I'm from Catalyst city";
		Map<String, Object> resp = new HashMap<String, Object>();
		resp.put("text", text);
		return resp;
	}
}