package enginuity.logger;

import enginuity.Settings;
import enginuity.logger.definition.EcuData;
import enginuity.logger.definition.EcuDataLoader;
import enginuity.logger.definition.EcuDataLoaderImpl;
import enginuity.logger.definition.EcuParameter;
import enginuity.logger.definition.EcuSwitch;
import enginuity.logger.io.serial.SerialPortRefresher;
import enginuity.logger.ui.DataRegistrationBroker;
import enginuity.logger.ui.DataRegistrationBrokerImpl;
import enginuity.logger.ui.EcuDataComparator;
import enginuity.logger.ui.EcuLoggerMenuBar;
import enginuity.logger.ui.LoggerDataTableModel;
import enginuity.logger.ui.MessageListener;
import enginuity.logger.ui.ParameterListTable;
import enginuity.logger.ui.ParameterListTableModel;
import enginuity.logger.ui.ParameterRow;
import enginuity.logger.ui.SerialPortComboBox;
import enginuity.logger.ui.UserProfile;
import enginuity.logger.ui.UserProfileImpl;
import enginuity.logger.ui.UserProfileItem;
import enginuity.logger.ui.UserProfileItemImpl;
import enginuity.logger.ui.UserProfileLoader;
import enginuity.logger.ui.UserProfileLoaderImpl;
import enginuity.logger.ui.handler.DashboardUpdateHandler;
import enginuity.logger.ui.handler.DataUpdateHandler;
import enginuity.logger.ui.handler.DataUpdateHandlerManager;
import enginuity.logger.ui.handler.DataUpdateHandlerManagerImpl;
import enginuity.logger.ui.handler.DataUpdateHandlerThreadWrapper;
import enginuity.logger.ui.handler.FileUpdateHandler;
import enginuity.logger.ui.handler.GraphUpdateHandler;
import enginuity.logger.ui.handler.LiveDataUpdateHandler;
import enginuity.logger.ui.handler.TableUpdateHandler;
import static enginuity.util.ParamChecker.checkNotNull;

import javax.swing.*;
import static javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED;
import static javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED;
import static javax.swing.JSplitPane.HORIZONTAL_SPLIT;
import static javax.swing.JSplitPane.VERTICAL_SPLIT;
import static javax.swing.JTabbedPane.BOTTOM;
import javax.swing.border.BevelBorder;
import static javax.swing.border.BevelBorder.LOWERED;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import java.awt.*;
import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.NORTH;
import static java.awt.BorderLayout.SOUTH;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import static java.util.Collections.sort;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
TODO: add better debug logging, preferably to a file and switchable (on/off)
TODO: finish dashboard tab
TODO: add configuration screen (log file destination, etc)
TODO: add user definable addresses
TODO: Clean up this class!
So much to do, so little time....
*/

public final class EcuLogger extends JFrame implements WindowListener, PropertyChangeListener, MessageListener {
    private static final String HEADING_PARAMETERS = "Parameters";
    private static final String HEADING_SWITCHES = "Switches";
    private Settings settings;
    private LoggerController controller;
    private JLabel statusBarLabel;
    private JTabbedPane tabbedPane;
    private SerialPortComboBox portsComboBox;
    private DataUpdateHandlerManager dataHandlerManager;
    private DataRegistrationBroker dataTabBroker;
    private ParameterListTableModel dataTabParamListTableModel;
    private ParameterListTableModel dataTabSwitchListTableModel;
    private DataUpdateHandlerManager graphHandlerManager;
    private DataRegistrationBroker graphTabBroker;
    private ParameterListTableModel graphTabParamListTableModel;
    private ParameterListTableModel graphTabSwitchListTableModel;
    private DataUpdateHandlerManager dashboardHandlerManager;
    private DataRegistrationBroker dashboardTabBroker;
    private ParameterListTableModel dashboardTabParamListTableModel;
    private ParameterListTableModel dashboardTabSwitchListTableModel;
    private FileUpdateHandler fileUpdateHandler;
    private LoggerDataTableModel dataTableModel;
    private LiveDataUpdateHandler liveDataUpdateHandler;
    private JPanel graphPanel;
    private GraphUpdateHandler graphUpdateHandler;
    private JPanel dashboardPanel;
    private DashboardUpdateHandler dashboardUpdateHandler;

    public EcuLogger(String title, Settings settings) {
        super(title);
        bootstrap(title, settings);
        initControllerListeners();
        startPortRefresherThread();
        initUserInterface();
        initDataUpdateHandlers();
        reloadUserProfile(settings.getLoggerProfileFilePath());
    }

    private void bootstrap(String title, Settings settings) {
        checkNotNull(title, settings);
        this.settings = settings;
        controller = new LoggerControllerImpl(settings, this);
        statusBarLabel = new JLabel(title);
        tabbedPane = new JTabbedPane(BOTTOM);
        portsComboBox = new SerialPortComboBox(settings);
        dataHandlerManager = new DataUpdateHandlerManagerImpl();
        dataTabBroker = new DataRegistrationBrokerImpl(controller, dataHandlerManager);
        dataTabParamListTableModel = new ParameterListTableModel(dataTabBroker, HEADING_PARAMETERS);
        dataTabSwitchListTableModel = new ParameterListTableModel(dataTabBroker, HEADING_SWITCHES);
        graphHandlerManager = new DataUpdateHandlerManagerImpl();
        graphTabBroker = new DataRegistrationBrokerImpl(controller, graphHandlerManager);
        graphTabParamListTableModel = new ParameterListTableModel(graphTabBroker, HEADING_PARAMETERS);
        graphTabSwitchListTableModel = new ParameterListTableModel(graphTabBroker, HEADING_SWITCHES);
        dashboardHandlerManager = new DataUpdateHandlerManagerImpl();
        dashboardTabBroker = new DataRegistrationBrokerImpl(controller, dashboardHandlerManager);
        dashboardTabParamListTableModel = new ParameterListTableModel(dashboardTabBroker, HEADING_PARAMETERS);
        dashboardTabSwitchListTableModel = new ParameterListTableModel(dashboardTabBroker, HEADING_SWITCHES);
        fileUpdateHandler = new FileUpdateHandler(settings);
        dataTableModel = new LoggerDataTableModel();
        liveDataUpdateHandler = new LiveDataUpdateHandler(dataTableModel);
        graphPanel = new JPanel(new SpringLayout());
        graphUpdateHandler = new GraphUpdateHandler(graphPanel);
        dashboardPanel = new JPanel(new GridLayout(3, 3, 4, 4));
        dashboardUpdateHandler = new DashboardUpdateHandler(dashboardPanel);
    }

    private void initControllerListeners() {
        controller.addListener(dataTabBroker);
        controller.addListener(graphTabBroker);
        controller.addListener(dashboardTabBroker);
    }

    private void startPortRefresherThread() {
        Thread portRefresherThread = new Thread(new SerialPortRefresher(portsComboBox, controller));
        portRefresherThread.setDaemon(true);
        portRefresherThread.start();
    }

    private void initUserInterface() {
        // add menubar to frame
        setJMenuBar(buildMenubar());

        // setup main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(buildControlToolbar(), NORTH);
        mainPanel.add(buildTabbedPane(), CENTER);
        mainPanel.add(buildStatusBar(), SOUTH);

        // add to container
        getContentPane().add(mainPanel);
    }

    public void reloadUserProfile(String profileFilePath) {
        try {
            EcuDataLoader dataLoader = new EcuDataLoaderImpl();
            dataLoader.loadFromXml(settings.getLoggerConfigFilePath(), settings.getLoggerProtocol());
            loadEcuData(dataLoader, profileFilePath);
        } catch (Exception e) {
            e.printStackTrace();
            reportError(e);
        }
    }

    private void loadEcuData(EcuDataLoader dataLoader, String profileFilePath) {
        UserProfileLoader profileLoader = new UserProfileLoaderImpl();
        UserProfile profile = profileLoader.loadProfile(profileFilePath);
        List<EcuParameter> ecuParams = dataLoader.getEcuParameters();
        addConvertorUpdateListeners(ecuParams);
        clearParamTableModels();
        clearSwitchTableModels();
        loadEcuParams(ecuParams, profile);
        loadEcuSwitches(dataLoader.getEcuSwitches(), profile);
    }

    private void addConvertorUpdateListeners(List<EcuParameter> ecuParams) {
        for (EcuParameter ecuParam : ecuParams) {
            ecuParam.addConvertorUpdateListener(fileUpdateHandler);
            ecuParam.addConvertorUpdateListener(liveDataUpdateHandler);
            ecuParam.addConvertorUpdateListener(graphUpdateHandler);
            ecuParam.addConvertorUpdateListener(dashboardUpdateHandler);
        }
    }

    private void clearParamTableModels() {
        stopLogging();
        dataTabParamListTableModel.clear();
        graphTabParamListTableModel.clear();
        dashboardTabParamListTableModel.clear();
    }

    private void clearSwitchTableModels() {
        stopLogging();
        dataTabSwitchListTableModel.clear();
        graphTabSwitchListTableModel.clear();
        dashboardTabSwitchListTableModel.clear();
    }

    private void loadEcuParams(List<EcuParameter> ecuParams, UserProfile profile) {
        sort(ecuParams, new EcuDataComparator());
        for (EcuParameter ecuParam : ecuParams) {
            if (profile == null || profile.contains(ecuParam)) {
                setDefaultUnits(profile, ecuParam);
                dataTabParamListTableModel.addParam(ecuParam, isSelectedOnLiveDataTab(profile, ecuParam));
                graphTabParamListTableModel.addParam(ecuParam, isSelectedOnGraphTab(profile, ecuParam));
                dashboardTabParamListTableModel.addParam(ecuParam, isSelectedOnDashTab(profile, ecuParam));
            }
        }
    }

    private void loadEcuSwitches(List<EcuSwitch> ecuSwitches, UserProfile profile) {
        sort(ecuSwitches, new EcuDataComparator());
        for (EcuSwitch ecuSwitch : ecuSwitches) {
            if (profile == null || profile.contains(ecuSwitch)) {
                dataTabSwitchListTableModel.addParam(ecuSwitch, isSelectedOnLiveDataTab(profile, ecuSwitch));
                graphTabSwitchListTableModel.addParam(ecuSwitch, isSelectedOnGraphTab(profile, ecuSwitch));
                dashboardTabSwitchListTableModel.addParam(ecuSwitch, isSelectedOnDashTab(profile, ecuSwitch));
            }
        }
    }

    private void setDefaultUnits(UserProfile profile, EcuParameter ecuParam) {
        if (profile != null) {
            try {
                ecuParam.selectConvertor(profile.getSelectedConvertor(ecuParam));
            } catch (Exception e) {
                reportError(e);
            }
        }
    }

    private boolean isSelectedOnLiveDataTab(UserProfile profile, EcuData ecuData) {
        return profile != null && profile.isSelectedOnLiveDataTab(ecuData);
    }

    private boolean isSelectedOnGraphTab(UserProfile profile, EcuData ecuData) {
        return profile != null && profile.isSelectedOnGraphTab(ecuData);
    }

    private boolean isSelectedOnDashTab(UserProfile profile, EcuData ecuData) {
        return profile != null && profile.isSelectedOnDashTab(ecuData);
    }

    public UserProfile getCurrentProfile() {
        Map<String, UserProfileItem> paramProfileItems = getProfileItems(dataTabParamListTableModel.getParameterRows(),
                graphTabParamListTableModel.getParameterRows(), dashboardTabParamListTableModel.getParameterRows());
        Map<String, UserProfileItem> switchProfileItems = getProfileItems(dataTabSwitchListTableModel.getParameterRows(),
                graphTabSwitchListTableModel.getParameterRows(), dashboardTabSwitchListTableModel.getParameterRows());
        return new UserProfileImpl(paramProfileItems, switchProfileItems);
    }

    private Map<String, UserProfileItem> getProfileItems(List<ParameterRow> dataTabRows, List<ParameterRow> graphTabRows, List<ParameterRow> dashTabRows) {
        Map<String, UserProfileItem> profileItems = new HashMap<String, UserProfileItem>();
        for (ParameterRow dataTabRow : dataTabRows) {
            String id = dataTabRow.getEcuData().getId();
            String units = dataTabRow.getEcuData().getSelectedConvertor().getUnits();
            boolean dataTabSelected = dataTabRow.getSelected();
            boolean graphTabSelected = isEcuDataSelected(id, graphTabRows);
            boolean dashTabSelected = isEcuDataSelected(id, dashTabRows);
            profileItems.put(id, new UserProfileItemImpl(units, dataTabSelected, graphTabSelected, dashTabSelected));
        }
        return profileItems;
    }

    private boolean isEcuDataSelected(String id, List<ParameterRow> parameterRows) {
        for (ParameterRow row : parameterRows) {
            if (id.equals(row.getEcuData().getId())) {
                return row.getSelected();
            }
        }
        return false;
    }

    private void initDataUpdateHandlers() {
        DataUpdateHandler threadedFileUpdateHandler = startHandlerInThread(fileUpdateHandler);
        dataHandlerManager.addHandler(startHandlerInThread(liveDataUpdateHandler));
        dataHandlerManager.addHandler(threadedFileUpdateHandler);
        dataHandlerManager.addHandler(startHandlerInThread(TableUpdateHandler.getInstance()));
        graphHandlerManager.addHandler(startHandlerInThread(graphUpdateHandler));
        graphHandlerManager.addHandler(threadedFileUpdateHandler);
        dashboardHandlerManager.addHandler(startHandlerInThread(dashboardUpdateHandler));
        dashboardHandlerManager.addHandler(threadedFileUpdateHandler);
    }

    private DataUpdateHandler startHandlerInThread(DataUpdateHandler handler) {
        DataUpdateHandlerThreadWrapper runnableHandler = new DataUpdateHandlerThreadWrapper(handler);
        Thread thread = new Thread(runnableHandler);
        thread.setDaemon(true);
        thread.start();
        return runnableHandler;
    }

    private JComponent buildTabbedPane() {
        tabbedPane.add("Data", buildSplitPane(buildParamListPane(dataTabParamListTableModel, dataTabSwitchListTableModel), buildDataTab()));
        tabbedPane.add("Graph", buildSplitPane(buildParamListPane(graphTabParamListTableModel, graphTabSwitchListTableModel), buildGraphTab()));
        tabbedPane.add("Dashboard", buildSplitPane(buildParamListPane(dashboardTabParamListTableModel, dashboardTabSwitchListTableModel), buildDashboardTab()));
        return tabbedPane;
    }

    private JComponent buildParamListPane(TableModel paramListTableModel, TableModel switchListTableModel) {
        JScrollPane paramList = new JScrollPane(buildParamListTable(paramListTableModel), VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED);
        JScrollPane switchList = new JScrollPane(buildParamListTable(switchListTableModel), VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED);
        JSplitPane splitPane = new JSplitPane(VERTICAL_SPLIT, paramList, switchList);
        splitPane.setDividerSize(2);
        splitPane.setDividerLocation(300);
        return splitPane;
    }

    private JTable buildParamListTable(TableModel tableModel) {
        JTable paramListTable = new ParameterListTable(tableModel);
        changeColumnWidth(paramListTable, 0, 20, 55, 55);
        changeColumnWidth(paramListTable, 2, 50, 250, 80);
        return paramListTable;
    }

    private void changeColumnWidth(JTable paramListTable, int colIndex, int minWidth, int maxWidth, int preferredWidth) {
        TableColumn column = paramListTable.getColumnModel().getColumn(colIndex);
        column.setMinWidth(minWidth);
        column.setMaxWidth(maxWidth);
        column.setPreferredWidth(preferredWidth);
    }

    private JComponent buildStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.add(statusBarLabel, CENTER);
        statusBar.setBorder(new BevelBorder(LOWERED));
        return statusBar;
    }

    private JSplitPane buildSplitPane(JComponent leftComponent, JComponent rightComponent) {
        JSplitPane splitPane = new JSplitPane(HORIZONTAL_SPLIT, leftComponent, rightComponent);
        splitPane.setDividerSize(2);
        splitPane.setDividerLocation(300);
        splitPane.addPropertyChangeListener(this);
        return splitPane;
    }

    private JMenuBar buildMenubar() {
        return new EcuLoggerMenuBar(this);
    }

    private JPanel buildControlToolbar() {
        JPanel controlPanel = new JPanel(new FlowLayout());
        controlPanel.add(buildStartButton());
        controlPanel.add(buildStopButton());
        controlPanel.add(buildPortsComboBox());
        return controlPanel;
    }

    private JComboBox buildPortsComboBox() {
        portsComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                settings.setLoggerPort((String) portsComboBox.getSelectedItem());
                stopLogging();
            }
        });
        return portsComboBox;
    }

    private JButton buildStartButton() {
        JButton startButton = new JButton("Start");
        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                startLogging();
            }
        });
        return startButton;
    }

    private JButton buildStopButton() {
        JButton stopButton = new JButton("Stop");
        stopButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                stopLogging();
            }
        });
        return stopButton;
    }

    private JComponent buildDataTab() {
        return new JScrollPane(new JTable(dataTableModel), VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER);
    }

    private JComponent buildGraphTab() {
        return new JScrollPane(graphPanel, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED);
    }

    private JComponent buildDashboardTab() {
        //return new JScrollPane(dashboardPanel, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER);
        return dashboardPanel;
    }

    public void windowOpened(WindowEvent windowEvent) {
    }

    public void windowClosing(WindowEvent windowEvent) {
        handleExit();
    }

    public void windowClosed(WindowEvent windowEvent) {
    }

    public void windowIconified(WindowEvent windowEvent) {
    }

    public void windowDeiconified(WindowEvent windowEvent) {
    }

    public void windowActivated(WindowEvent windowEvent) {
    }

    public void windowDeactivated(WindowEvent windowEvent) {
    }

    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
    }

    public void loadProfile(File profileFile) {
        //TODO: Finish profile loading from file!!
    }

    public void startLogging() {
        settings.setLoggerPort((String) portsComboBox.getSelectedItem());
        controller.start();
    }

    public void stopLogging() {
        controller.stop();
    }

    public void handleExit() {
        try {
            try {
                stopLogging();
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } finally {
                cleanUpUpdateHandlers();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            rememberWindowProperties();
        }
    }

    private void rememberWindowProperties() {
        settings.setLoggerWindowMaximized(getExtendedState() == MAXIMIZED_BOTH);
        settings.setLoggerWindowSize(getSize());
        settings.setLoggerWindowLocation(getLocation());
    }

    private void cleanUpUpdateHandlers() {
        dataHandlerManager.cleanUp();
        graphHandlerManager.cleanUp();
        dashboardHandlerManager.cleanUp();
    }

    public Settings getSettings() {
        return settings;
    }

    public void reportMessage(String message) {
        if (message != null) {
            statusBarLabel.setText(message);
        }
    }

    public void reportError(String error) {
        if (error != null) {
            JOptionPane.showMessageDialog(null, error);
        }
    }

    public void reportError(Exception e) {
        if (e != null) {
            String message = e.getMessage();
            int i = message.indexOf(": ");
            if (i >= 0) {
                message = message.substring(i + 2);
            }
            reportError(message);
        }
    }

    //**********************************************************************


    public static void main(String... args) {
        startLogger(EXIT_ON_CLOSE, new Settings());
    }

    public static void startLogger(final int defaultCloseOperation, final Settings settings) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI(defaultCloseOperation, settings);
            }
        });
    }

    private static void createAndShowGUI(int defaultCloseOperation, Settings settings) {
        //set look and feel
        setLookAndFeel();

        //make sure we have nice window decorations.
        setDefaultLookAndFeelDecorated(true);
        JDialog.setDefaultLookAndFeelDecorated(true);

        //instantiate the controlling class.
        EcuLogger ecuLogger = new EcuLogger("Enginuity ECU Logger", settings);

        //set remaining window properties
        ecuLogger.setSize(settings.getLoggerWindowSize());
        ecuLogger.setIconImage(new ImageIcon("./graphics/enginuity-ico.gif").getImage());
        ecuLogger.setDefaultCloseOperation(defaultCloseOperation);
        ecuLogger.addWindowListener(ecuLogger);

        //display the window
        ecuLogger.setLocation(settings.getLoggerWindowLocation());
        if (settings.isWindowMaximized()) {
            ecuLogger.setExtendedState(MAXIMIZED_BOTH);
        }
        ecuLogger.setVisible(true);
    }

    private static void setLookAndFeel() {
        try {
            //use the system look and feel.
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
