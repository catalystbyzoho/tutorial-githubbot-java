package com.handlers;

import java.util.HashMap;
import java.util.Map;

import com.zc.cliq.objects.FormChangeResponse;
import com.zc.cliq.objects.FormDynamicFieldResponse;
import com.zc.cliq.objects.Message;
import com.zc.cliq.objects.WidgetSection;
import com.zc.cliq.requests.ButtonFunctionRequest;
import com.zc.cliq.requests.FormFunctionRequest;
import com.zc.cliq.requests.WidgetFunctionRequest;
import com.zc.cliq.util.ZCCliqUtil;

public class FunctionHandler implements com.zc.cliq.interfaces.FunctionHandler
{

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> buttonFunctionHandler(ButtonFunctionRequest req) throws Exception
	{
		String text;
		if(req.getName().equals("authenticate")){
			text = ((HashMap<String,Object>)req.getArguments().get("input")).get("token").toString();
		}
		else{
			text = "Button function executed";
		}
		Message msg = Message.getInstance(text);
		return ZCCliqUtil.toMap(msg);
	}
	
	@Override
	public Map<String, Object> formSubmitHandler(FormFunctionRequest req) throws Exception 
	{
		return new HashMap<>();
	}

	@Override
	public FormChangeResponse formChangeHandler(FormFunctionRequest req) throws Exception
	{
		FormChangeResponse resp = FormChangeResponse.getInstance();
		return resp;
	}

	@Override
	public FormDynamicFieldResponse formDynamicFieldHandler(FormFunctionRequest req) throws Exception
	{
		FormDynamicFieldResponse resp = FormDynamicFieldResponse.getInstance();
		return resp;
	}

	@Override
	public Map<String, Object> widgetButtonHandler(WidgetFunctionRequest req) throws Exception
	{
		return new HashMap<>();
	}

	private WidgetSection getButtonsSection()
	{
		WidgetSection buttonSection = WidgetSection.getInstance();
		return buttonSection;
	}
}