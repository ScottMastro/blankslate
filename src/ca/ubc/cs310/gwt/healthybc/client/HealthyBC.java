package ca.ubc.cs310.gwt.healthybc.client;

import java.util.ArrayList;
import java.util.Date;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.maps.client.LoadApi;
import com.google.gwt.maps.client.LoadApi.LoadLibrary;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;


/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class HealthyBC implements EntryPoint {

	private static HealthyBC singleton;

	/**
	 * The message displayed to the user when the server cannot be reached or
	 * returns an error.
	 */
	@SuppressWarnings("unused")
	private static final String SERVER_ERROR = "An error occurred while "
			+ "attempting to contact the server. Please check your network "
			+ "connection and try again.";

	private static final String WELCOME_STRING = "Welcome to HealthyBC";

	private static final long DURATION = 1000 * 60 * 60 * 24;
	private DockLayoutPanel dock;
	private DockLayoutPanel mapTableDock;
	private TabLayoutPanel tabs;
	private ArrayList<String> tabNames;
	private boolean showAdminTools = false;
	private String currentUser = "";

	// Social Login vars
	private static Widget socialLoginPanel;
	private static Anchor logoutAnchor = new Anchor();

	public static HealthyBC get() {
		return singleton;
	}

	public void log(String msg) {
		Log.debug(msg);
	}

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		singleton = this;

		//handleRedirect();
		String username = Cookies.getCookie("HBC_username");

		if (username != null){
			currentUser = username.trim();
			if (currentUser.equalsIgnoreCase("admin")) {
				showAdminTools = true;
			}
			else {
				showAdminTools = false;
			}
			//History.newItem("homepage");
			init();
		} else {
			//History.newItem("login");
			login();
		}

		/*History.addValueChangeHandler(new ValueChangeHandler<String>() {
                        public void onValueChange(ValueChangeEvent<String> event) {
                                String historyToken = event.getValue();
                                System.out.println(historyToken);
                        }
         });*/

	}


	private void handleRedirect()
	{
		if (SocialLogin.redirected())
		{
			verifySocialUser();
		}
	}

	private void verifySocialUser()
	{
		final String authProviderName = SocialLogin.getAuthProviderNameFromCookie();
		final int authProvider = SocialLogin.getAuthProviderFromCookieAsInt();
		log("Verifying " + authProviderName + " user ...");

		new LoginCallbackAsync<SocialUser>()
		{
			@Override
			public void onSuccess(SocialUser result)
			{
				SocialLogin.saveSessionId(result.getSessionId());

				String name = "";
				if (result.getName() != null)
				{
					name = result.getName();
					SocialLogin.saveName(name, authProvider);
				}

				// TODO: remove alert in final version
				Window.alert(result.getJson());				
			}

			@Override
			protected void callService(AsyncCallback<SocialUser> cb)
			{
				try
				{
					final Credential credential = SocialLogin.getCredential();
					if (credential == null)
					{
						log("verifySocialUser: Could not get credential for " + authProvider + " user");
						return;
					}
					OAuthLoginService.Util.getInstance().verifySocialUser(credential,cb);
				}
				catch (Exception e)
				{
					Window.alert(e.getMessage());
				}
			}

			@Override
			public void onFailure(Throwable caught)
			{
				Window.alert("Coult not verify" + authProvider + " user." + caught);
			}
		}.go("Verifying " + authProviderName + " user..");
	}

	/**
	 * Authorizes the user
	 */
	private void login(){

		VerticalPanel rtpanel = new VerticalPanel();

		rtpanel.add(new HTML("<h2> Healthy BC : Walk-in Clinics </h2> <br/>"));

		HorizontalPanel hpanel = new HorizontalPanel();
		hpanel.setHeight("520px");

		// Create Login form
		hpanel.add(createLoginForm());

		// Create registration form
		hpanel.add(createRegisterForm()); 

		rtpanel.add(hpanel);
		rtpanel.add(new HTML("Created by: The Blank Slate Team (CPSC 310)"));

		RootPanel.get().add(rtpanel, 20, 20);    

	}

	/**
	 * Create a login form
	 */
	private final FormPanel createLoginForm(){

		final FormPanel form = FormBuilder.createLoginForm();

		form.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {
			public void onSubmitComplete(SubmitCompleteEvent event) {
				// When the form submission is successfully completed, this event is
				// fired. Assuming the service returned a response of type text/html,
				// we can get the result text here (see the FormPanel documentation for
				// further explanation).
				if (event.getResults().trim().startsWith("success")){
					Date expires = new Date(System.currentTimeMillis() + DURATION);
					String user = event.getResults().split(":")[1].trim();
					Cookies.setCookie("HBC_username", user, expires, null, "/", false);
					RootPanel.get().clear();
					//History.newItem("homepage");
					init();
				} else if (event.getResults().trim().startsWith("admin")){
					Date expires = new Date(System.currentTimeMillis() + DURATION);
					String user = event.getResults().split(":")[1].trim();
					Cookies.setCookie("HBC_username", user, expires, null, "/", false);
					RootPanel.get().clear();
					showAdminTools = true;
					//History.newItem("homepage");
					init();
				} else {
					Window.alert("Error: Login failed!");
				}
			}
		});

		return form;
	}

	/**
	 * Create Registration form
	 */
	private final FormPanel createRegisterForm(){

		final FormPanel form = FormBuilder.createRegisterForm();

		form.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {
			public void onSubmitComplete(SubmitCompleteEvent event) {
				// When the form submission is successfully completed, this event is
				// fired. Assuming the service returned a response of type text/html,
				// we can get the result text here (see the FormPanel documentation for
				// further explanation).
				if (event.getResults().trim().startsWith("success")){
					Window.alert("New user created.");
					Date expires = new Date(System.currentTimeMillis() + DURATION);
					String user = event.getResults().split(":")[1].trim();
					Cookies.setCookie("HBC_username", user, expires, null, "/", false);

					RootPanel.get().clear();
					//History.newItem("homepage");
					init();
				} else {
					Window.alert("Error: Could not create user. Username taken.");
				}
			}
		});

		return form;
	}

	/**
	 * Sets up the interface for the main page.
	 */
	private void init() {

		dock = new DockLayoutPanel(Unit.PCT);
		mapTableDock = new DockLayoutPanel(Unit.PCT);

		tabs = new TabLayoutPanel(2.5, Unit.EM);
		tabNames = new ArrayList<String>();

		socialLoginPanel = SocialLogin.createLoginPanel();

		// add home page & logout button
		OptionsTab options = new OptionsTab(this);
		tabs.add(options.getOptionsTab(), "Home");
		tabs.add(socialLoginPanel, "Account");

		tabNames.add("Home");
		tabNames.add("Account");

		createMap(null, null, null, null);
		createTable(null, null);
		createUploadForm();

		dock.addWest(mapTableDock, 30);
		dock.addEast(tabs, 70);

		RootLayoutPanel r = RootLayoutPanel.get();
		r.add(dock);
		r.forceLayout();
	}

	public void search(String searchBy, String searchKey){
		mapTableDock.clear();
		createTable(searchBy, searchKey);
		createMap(searchBy, searchKey, null, null);
	}

	public void setAddress(Double lat, Double lon){
		mapTableDock.clear();
		createTable(null, null);
		createMap(null, null, lat, lon);
	}

	// --------------------------------------------------------------
	// Create Map
	// --------------------------------------------------------------

	/**
	 * Calls required methods to create the Google map interface
	 */
	private void createMap(String searchBy, String searchKey, Double lat, Double lon) {
		loadMapAPI(searchBy, searchKey, lat, lon);
	}

	/**
	 * Initiates Google Map API

	 */
	private void loadMapAPI(final String searchBy, final String searchKey, final Double lat, final Double lon) {
		boolean sensor = false;

		// Load libraries needed for maps
		ArrayList<LoadLibrary> loadLibraries = new ArrayList<LoadApi.LoadLibrary>();

		// need this API to draw overlays on the map
		loadLibraries.add(LoadLibrary.DRAWING);

		Runnable onLoad = new Runnable() {
			@Override
			public void run() {
				// callback on successful API load
				if(lat == null || lon == null)
					buildMap(searchBy, searchKey);
				else
					buildMap(null, null, lat, lon);
			}
		};

		LoadApi.go(onLoad, loadLibraries, sensor);
	}

	/**
	 * On successful API load, retrieves data and constructs map
	 */
	private void buildMap(String searchBy, String searchKey) {
		ClinicDataFetcherAsync clinicFetcher = GWT.create(ClinicDataFetcher.class);

		MapBuilder callback = new MapBuilder(this);
		clinicFetcher.mapInfo(searchBy, searchKey, callback);
	}

	/**
	 * On successful API load, retrieves data and constructs map
	 */
	public void buildMap(String searchBy, String searchKey, Double latCentre, Double lonCentre) {
		ClinicDataFetcherAsync clinicFetcher = GWT.create(ClinicDataFetcher.class);

		MapBuilder callback = new MapBuilder(this, latCentre, lonCentre);
		clinicFetcher.mapInfo(searchBy, searchKey, callback);
	}

	/**
	 * Called when map is ready to be displayed from MapBuilder
	 * @param mapContainer is the map to display
	 */
	public void addMap(SimplePanel mapContainer){
		mapTableDock.addSouth(mapContainer, 50);
	}


	// --------------------------------------------------------------
	// Create Table
	// --------------------------------------------------------------

	/**
	 * Calls required methods to create the table
	 */
	private void createTable(String searchBy, String searchKey) {
		buildTable(searchBy, searchKey);
	}

	/**
	 * Retrieves data and constructs the table
	 */
	private void buildTable(String searchBy, String searchKey) {
		ClinicDataFetcherAsync tableFetcher = GWT.create(ClinicDataFetcher.class);

		TableInfoListCallback callback = new TableInfoListCallback();
		tableFetcher.tableInfo(searchBy, searchKey, callback);
	}

	/**
	 * Calls the server to retrieve data
	 */
	private class TableInfoListCallback implements AsyncCallback<ArrayList<TableInfo>> {
		@Override
		public void onFailure(Throwable caught) {
			caught.printStackTrace();
		}

		@Override
		public void onSuccess(ArrayList<TableInfo> result) {
			CellTable<TableInfo> table = new TableBuilder().buildTable(result);

			final SingleSelectionModel<TableInfo> ssm = new SingleSelectionModel<TableInfo>();
			table.setSelectionModel(ssm);

			ssm.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
				@Override
				public void onSelectionChange(final SelectionChangeEvent event)
				{

					final TableInfo selected = ssm.getSelectedObject();
					getTabFromTableInfo(selected);
				}
			});

			ScrollPanel panel = new ScrollPanel(table);
			panel.setAlwaysShowScrollBars(true);

			mapTableDock.addNorth(panel,50);
		}
	}

	// --------------------------------------------------------------
	// Create Upload Form
	// --------------------------------------------------------------

	/**
	 * Sets up form to browse for and send local file to servlet
	 */
	private void createUploadForm(){
		HorizontalPanel hp = new HorizontalPanel();
		hp.getElement().setAttribute("cellpadding", "5");
		if (showAdminTools == true ){
			HTML uploadForm = new HTML("<form method='POST' action='/uploadURL'/>"
					+ "Upload data from URL: <input name='urlstring' type='text' /> &nbsp;"
					+ "<input type='submit' value='Submit' /> </form> <br />"
					+ "<form enctype='multipart/form-data' "
					+ "method='POST' action='/uploadServlet'/>"
					+ "Upload CSV data : <input name='userfile1' type='file' />"
					+ "<input type='submit' value='Submit' /> </form>");
			hp.add(uploadForm);
			hp.setCellHorizontalAlignment(uploadForm, HasHorizontalAlignment.ALIGN_LEFT);

		} else {
			HTML footer = new HTML("<b>Team Members:</b> Alex Tan, Ben Liang, Dhananjay Bhaskar and Scott Mastromatteo <br/>"
					+ "<br/> <b>Contact Us:</b> <a href='mailto:theblanksl8@gmail.com'>theblanksl8 AT gmail DOT com</a> &nbsp;"
					+ "&copy; 2014 The Blank Slate Team<br />");
			hp.add(footer);
			hp.setCellHorizontalAlignment(footer, HasHorizontalAlignment.ALIGN_LEFT);
		}

		Button logoutButton = new Button("Logout", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				Cookies.removeCookie("HBC_username");
				RootPanel.get().clear();
				login();
			}
		});
		hp.add(logoutButton);
		hp.setCellHorizontalAlignment(logoutButton, HasHorizontalAlignment.ALIGN_RIGHT);
		hp.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		dock.addSouth(hp, 15);
	}


	// --------------------------------------------------------------
	// Create Tabs
	// --------------------------------------------------------------

	private boolean removeTab(String name){

		for (int i = 0; i <= tabNames.size() -1; i++){
			if (tabNames.get(i).equals(name)){

				tabs.remove(i);
				tabNames.remove(i);
				return true;
			}
		}

		return false;
	}

	private int findTab(String name){

		for (int i = 0; i <= tabNames.size() -1; i++){
			if (tabNames.get(i).equals(name))
				return i;
		}

		return 0;
	}

	private void getTabFromTableInfo(TableInfo ti){
		TabFetcherAsync tabFetcher = GWT.create(TabFetcher.class);

		ClinicTabCallback callback = new ClinicTabCallback();
		tabFetcher.clinicTabInfo(ti, callback);
	}

	public void getTabFromMapInfo(MapInfo mi){
		TabFetcherAsync tabFetcher = GWT.create(TabFetcher.class);

		ClinicTabCallback callback = new ClinicTabCallback();
		tabFetcher.clinicTabInfo(mi, callback);
	}

	/**
	 * Calls the server to retrieve data
	 */
	private class ClinicTabCallback implements AsyncCallback<ArrayList<ClinicTabInfo>> {
		@Override
		public void onFailure(Throwable caught) {
			caught.printStackTrace();

		}

		@Override
		public void onSuccess(ArrayList<ClinicTabInfo> result) {
			if(result == null){
				Window.alert("Could not find information about this clinic");                          
			}
			else{

				ClinicTabInfo t = result.get(0);
				removeTab("Clinic Information");
				DockLayoutPanel clinicPanel = new DockLayoutPanel(Unit.PCT);

				boolean hasEmail = !(t.getEmail() == null) && !t.getEmail().isEmpty();
				String emailString = "";
				if(hasEmail)
					emailString = "<LI><b>Email: </b>" + t.getEmail();

				HTML info = new HTML("<br>"
						+ "<h1 style='margin:0px'>" + t.getName() + "</h1>"
						+ "<font size='4'>"
						+ "<center>" + t.getAddress() + " " + t.getPostalCode() + "</center><br>"
						+ "<UL>"
						+ "<LI><b>Hours: </b>" + t.getHours() + "<br><br>"
						+ "<LI><b>Available Languages: </b>" + t.getLanguages() 
						+ "<LI><b>Phone: </b>" + t.getPhone() + "<br>"
						+ emailString
						+ "</UL></font>"
						);

				clinicPanel.addWest(info, 60);

				if(hasEmail){
					VerticalPanel emailPanel = new VerticalPanel();

					EmailBox email = new EmailBox(t.getEmail());
					emailPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

					emailPanel.add(new HTML("<h2 style='color:gray'>Compose email to clinic</h2>"));
					emailPanel.add(email.getBox());				
					clinicPanel.addEast(emailPanel, 40);
				}

				tabs.add(clinicPanel, "Clinic Information");

				tabNames.add(tabs.getWidgetCount() -1, "Clinic Information");
				tabs.selectTab(findTab("Clinic Information"));

				removeTab("View Ratings");

				RatingTab ratingTab = new RatingTab(t.getRefID(), currentUser);

				tabs.add(ratingTab.getRatingTab(), "View Ratings");
				tabNames.add(tabs.getWidgetCount() -1, "View Ratings");

			}
		}
	}
}
