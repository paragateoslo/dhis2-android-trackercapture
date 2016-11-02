package org.hisp.dhis.android.app.views.dashboard.navigation.event;

import org.hisp.dhis.android.app.views.dashboard.TeiDashboardPresenter;
import org.hisp.dhis.client.sdk.models.common.state.Action;
import org.hisp.dhis.client.sdk.models.common.state.State;
import org.hisp.dhis.client.sdk.models.event.Event;
import org.hisp.dhis.client.sdk.models.program.ProgramStage;
import org.hisp.dhis.client.sdk.models.trackedentity.TrackedEntityDataValue;
import org.hisp.dhis.client.sdk.ui.bindings.views.View;
import org.hisp.dhis.client.sdk.ui.models.ExpansionPanel;
import org.hisp.dhis.client.sdk.ui.models.ReportEntity;
import org.hisp.dhis.client.sdk.utils.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class TeiProgramStagePresenterImpl implements TeiProgramStagePresenter {
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private final TeiDashboardPresenter teiDashboardPresenter;
    private TeiProgramStageView teiProgramStageView;

    public TeiProgramStagePresenterImpl(TeiDashboardPresenter teiDashboardPresenter) {
        this.teiDashboardPresenter = teiDashboardPresenter;
    }

    @Override
    public void drawProgramStages(String enrollmentUid) {
        teiProgramStageView.drawProgramStages(getDummyProgramStages());
    }

    @Override
    public void onEventClicked(String eventUid) {
        teiDashboardPresenter.showDataEntryForEvent(eventUid);
        teiDashboardPresenter.hideMenu();
    }

    @Override
    public void attachView(View view) {
        teiProgramStageView = (TeiProgramStageView) view;
    }

    @Override
    public void detachView() {
        teiProgramStageView = null;
    }

    List<ExpansionPanel> getDummyProgramStages() {
        List<ExpansionPanel> expansionPanels = new ArrayList<>();
        //TODO: find the view holder for ReportEntities and update the icon according to the status.
        //TODO: Fix the "123"'s and so on to be the actual uid's and display the event name from the hashMap instead.
        List<ProgramStage> programStages = new ArrayList<>();
        List<Event> events = new ArrayList<>();

        //Fill random data \0/ :
        // Make test ProgramStages :
        for (int i = 0; i < 6 + new Random().nextInt(20); i++) {
            ProgramStage p = new ProgramStage();
            p.setDisplayName("Program Stage " + i);
            p.setName("Program Stage " + i);
            p.setUId("p" + i);
            programStages.add(p);
            //if(new Random().nextInt(3) == 1) {
            p.setRepeatable(true);
            //}
        }

        for (int i = 0; i < 100 + new Random().nextInt(400); i++) {
            Event e = new Event();
            e.setDisplayName("Event " + i);
            e.setUId("e" + i);
            e.setStatus(Event.EventStatus.values()[new Random().nextInt(Event.EventStatus.values().length)]);
            e.setProgramStage("p" + new Random().nextInt(programStages.size()));
            e.setEventDate(
                    DateTime.now()
                            .minusDays(new Random().nextInt(666))
                            .minusMonths(new Random().nextInt(66))
                            .minusYears(new Random().nextInt(66))
            );
            events.add(e);
        }
        expansionPanels = programStagesToExpansionPanel(programStages, events);
        return expansionPanels;
    }

    List<ExpansionPanel> programStagesToExpansionPanel(List<ProgramStage> stages, List<Event> events) {
        List<ExpansionPanel> expansionPanels = new ArrayList<>();

        for (ProgramStage programStage : stages) {
            // determine the type:
            ExpansionPanel.Type type = ExpansionPanel.Type.ACTION_ADD;
            if (!programStage.isRepeatable()) {
                type = ExpansionPanel.Type.ACTION_EDIT;
            }
            // make the Stage representation:
            ExpansionPanel current = new ExpansionPanel(programStage.getUId(), programStage.getDisplayName(), type);

            current.setChildren(eventsToReportEntityList(programStage, events));
            expansionPanels.add(current);
        }
        return expansionPanels;
    }

    /**
     * Returns a list of ReportEntities that corresponds to Event's in that program stage.
     *
     * @param programStage
     * @param events
     * @return
     */
    private List<ReportEntity> eventsToReportEntityList(ProgramStage programStage, List<Event> events) {
        List<ReportEntity> reportEntities = new ArrayList<>();
        if (events == null || events.isEmpty()) { // preventing additional work
            return reportEntities;
        }
        Collections.sort(events, Event.DATE_COMPARATOR); // sort events by eventDate
        Collections.reverse(events);
        // retrieve state map for given events
        // it is done synchronously
        //TODO: use the new sdk when out, get states from the sdk :
        //Map<Long, State> stateMap = eventInteractor.map(events).toBlocking().first();

        for (Event event : events) {
            if (programStage.getUId().equals(event.getProgramStage())) { //if event in prog stage:
                // syncStatus of event
                ReportEntity.SyncStatus syncStatus;
                // get state of event from database
                //TODO: get the state's form the map:
                //State state = stateMap.get(event.getId());
                //A mock up that picks a random action for now:
                State state = new State();
                state.setAction(Action.values()[new Random().nextInt(Action.values().length - 1)]);

                //logger.d(TAG, "State action for event " + event + " is " + state.getAction());
                switch (state.getAction()) {
                    case SYNCED: {
                        syncStatus = ReportEntity.SyncStatus.SENT;
                        break;
                    }
                    case TO_POST: {
                        syncStatus = ReportEntity.SyncStatus.TO_POST;
                        break;
                    }
                    case TO_UPDATE: {
                        syncStatus = ReportEntity.SyncStatus.TO_UPDATE;
                        break;
                    }
                    case ERROR: {
                        syncStatus = ReportEntity.SyncStatus.ERROR;
                        break;
                    }
                    default: {
                        throw new IllegalArgumentException(
                                "Unsupported event state: " + state.getAction());
                    }
                }
                //Map<String, String> dataElementToValueMap = mapDataElementToValue(event.getDataValues());
                Map<String, String> dataElementToValueMap = new HashMap<>();
                dataElementToValueMap.put(Event.EVENT_DATE_KEY, event.getEventDate().toString(DateTimeFormat.forPattern(DATE_FORMAT)));
                dataElementToValueMap.put(Event.EVENT_STATUS, event.getStatus().toString());
                //dataElementToValueMap.put("OrgUnit", event.getOrgUnit());
                reportEntities.add(new ReportEntity(event.getUId(), syncStatus, dataElementToValueMap));
            }
        }
        return reportEntities;
    }

    private Map<String, String> mapDataElementToValue(List<TrackedEntityDataValue> dataValues) {
        Map<String, String> dataElementToValueMap = new HashMap<>();
        if (dataValues != null && !dataValues.isEmpty()) {
            for (TrackedEntityDataValue dataValue : dataValues) {
                String value = !StringUtils.isEmpty(dataValue.getValue()) ? dataValue.getValue() : "";
                dataElementToValueMap.put(dataValue.getDataElement(), value);
            }
        }
        return dataElementToValueMap;
    }
}
