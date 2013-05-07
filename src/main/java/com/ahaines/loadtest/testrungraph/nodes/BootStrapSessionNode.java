package com.ahaines.loadtest.testrungraph.nodes;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ahaines.loadtest.request.RequestInfo;
import com.ahaines.loadtest.request.RequestType;
import com.ahaines.loadtest.response.ResponseInfo;
import com.ahaines.loadtest.response.StandardResponseTypes;
import com.ahaines.loadtest.testrungraph.BackLookTestNode;
import com.ahaines.loadtest.testrungraph.SingleTransitionTestNode;
import com.ahaines.loadtest.testrungraph.TestNode;

public class BootStrapSessionNode extends SingleTransitionTestNode<TestNode> implements SessionAwareNode{
	
	private static final Logger LOG = LoggerFactory.getLogger(BootStrapSessionNode.class);
	
	public static enum RequestTypes implements RequestType{
		BOOTSTRAP;
	}

	private final static String CALLBACK_PAGE = "fb/marlin/";
	private static final Pattern SESSION_REGEX_PATTERN = Pattern.compile("\"rpc_session_id\":\"([^\"]+)\"");
	private static final Pattern MANIFEST_VERSION_REGEX_PATTERN = Pattern.compile("\"pf_manifest_version\":\"([^\"]+)\"");
	
	private String sessionKey;
	private String manifestVersion;
	private final long userId;
	
	public BootStrapSessionNode(BackLookTestNode<? extends TestNode> nextNode, long userId) {
		super(nextNode);
		this.userId = userId;
	}
	
	public BootStrapSessionNode(long userId){
		this(null, userId);
	}
	
	@Override
	public RequestInfo getRequest() {

		String path = CALLBACK_PAGE+"?mockedUserToken=A_test_token&mockedUserId="+userId;
		return new RequestInfo(RequestTypes.BOOTSTRAP, path, new byte[]{}, "POST", "*/*");
	}

	@Override
	public void responseCallback(ResponseInfo response) {
		if (response.getResponseType() == StandardResponseTypes.RESPONSE_OK){
			String page = new String(response.getResponseContent());
			Matcher sessionKeyMatcher = SESSION_REGEX_PATTERN.matcher(page);
			Matcher manifestVersionMatcher = MANIFEST_VERSION_REGEX_PATTERN.matcher(page);
			try{
				sessionKeyMatcher.find();
				manifestVersionMatcher.find();

				sessionKey = sessionKeyMatcher.group(1);
				manifestVersion = manifestVersionMatcher.group(1);
				
				LOG.debug("sessionKey extracted as = {}",sessionKey);
				LOG.debug("manifestVersion extracted as = {}",manifestVersion);
			} catch (RuntimeException t){
				LOG.error("unable to parse response: "+page, t);
				throw t;
			}
		}
	}

	public String getSessionKey() {
		return sessionKey;
	}

	public String getManifestVersion() {
		return manifestVersion;
	}

}
