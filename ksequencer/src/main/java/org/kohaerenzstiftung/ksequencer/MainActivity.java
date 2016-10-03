package org.kohaerenzstiftung.ksequencer;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.kohaerenzstiftung.AsyncTaskResult;
import org.kohaerenzstiftung.ContextItemExecutor;
import org.kohaerenzstiftung.ContextMenuCreator;
import org.kohaerenzstiftung.Dialog;
import org.kohaerenzstiftung.HTTP;
import org.kohaerenzstiftung.HTTPServerRequest;
import org.kohaerenzstiftung.MenuActivity;
import org.kohaerenzstiftung.NextNumberGetter;
import org.kohaerenzstiftung.NumberPicker;
import org.kohaerenzstiftung.StandardActivity;
import org.kohaerenzstiftung.YesNoable;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;


public class MainActivity extends MenuActivity {

    //----------------------MainActivity member variables
    private NumberPickerDialog mNumberPickerDialog = null;
    private LinearLayout mStepsLayout;
    private Button mBarsButton;
    private Button mStepsPerBarButton;
    private Button mBarsPerPageButton;
    private Button mValuesButton;
    private Button mRandomiseButton;
    private Button mPreviousButton;
    private Button mNextButton;
    private Button mParentButton;
    private Button mChildrenButton;
    private Button mSiblingsButton;
    private Button mSaveButton;
    private Button mSubmitButton;
    private RealPattern mPattern;
    private int mBarsPerPage;
    private int mCurrentPage = 0;
    private boolean mHaveElements = false;
    private static final String SERVER_DEFAULT = "localhost";
    private static final int PORT_DEFAULT = 8080;
    private Button mServerButton;
    private String mServer = SERVER_DEFAULT;
    private int mPort = PORT_DEFAULT;
    private ServerDialog mServerDialog = null;
    private Button mTopButton;
    private RandomiseDialog mRandomiseDialog;
    private Random mRandom;
    private static final int C0 = 24;
    private Handler mHandler;
    private SetupPatternDialog mSetupPatternDialog;
    private PatternAddedExecutor mPatternAddedExecutor;
    private NewPatternHandler mNewPatternHandler;
    private StepValuesDialog mStepValuesDialog = null;
    private HTTPServerRequest.ThrowableRunnable mFailedRunnable =
            new HTTPServerRequest.ThrowableRunnable() {

                private Throwable mThrowable;

                @Override
                public void run() {
                    String message = mThrowable.getMessage();
                    String text;
                    if (message != null) {
                        text = mThrowable.getClass().getName() + ": " + message;
                    } else {
                        text = mThrowable.getClass().getName();
                    }
                    Toast.makeText(MainActivity.this,
                            text, Toast.LENGTH_LONG).show();
                }

                @Override
                public void setThrowable(Throwable throwable) {
                    mThrowable = throwable;
                }

            };
    private RootPattern mRootPattern = new RootPattern();
    private PatternListDialog mPatternListDialog;
    private SetStepsPerBarNextNumberGetter mSetStepsPerBarNextNumberGetter =
            new SetStepsPerBarNextNumberGetter();
    private SetBarsNextNumberGetter mSetBarsNextNumberGetter = new SetBarsNextNumberGetter();
    private StepValuesDialog getStepValuesDialog() {
        if (mStepValuesDialog == null) {
            mStepValuesDialog = new StepValuesDialog();
        }
        return mStepValuesDialog;
    }
    private ViewGroup.LayoutParams mHorizontalLayoutParams = new
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1);
    private NumberPickerDialogExecutor mSetBarsExecutor = new NumberPickerDialogExecutor() {

        @Override
        void execute(int bars) {
            MainActivity.this.setBars(bars);
        }
    };
    private NumberPickerDialogExecutor mSetBarsPerPageExecutor = new NumberPickerDialogExecutor() {

        @Override
        void execute(int bars) {
            MainActivity.this.setBarsPerPage(bars);
        }
    };
    private NumberPickerDialogExecutor mSetStepsPerBarExecutor = new NumberPickerDialogExecutor() {

        @Override
        void execute(int steps) {
            MainActivity.this.setStepsPerBar(steps);
        }
    };

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            MainActivity.this.onClick(view);
        }
    };
    private NextNumberGetter mSetBarsPerPageNextNumberGetter = new NextNumberGetter() {
        public int getNext(int current, boolean up) {
            int result = 1;
            if (up) {
                result = current + 1;
                if (result < 0) {
                    result = current;
                }
            } else if (current > 1) {
                result = current - 1;
            }
            return result;
        }
    };






    //----------------------MainActivity overridden methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setServerFromString(getServerString());
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
    @Override
    protected void readArguments(Bundle extras) {
        RealPattern pattern = null;

        if (extras != null) {
            String filename = extras.getString("filename", null);
            if (filename != null) {
                pattern = readPattern(mRootPattern, filename);
            }
        }

        boolean created = false;
        if (pattern == null) {
            pattern = new RealPattern(getResources().getString(R.string.untitled), mRootPattern);
            created = true;
        }
        enterPattern(pattern);
        setBarsPerPage(1);
        if (created) {
            Handler handler = getHandler();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    SetupPatternDialog setupPatternDialog = getSetupPatternDialog();
                    setupPatternDialog.reset(mPattern, null);
                    showDialog(setupPatternDialog);
                }
            });

        }
    }
    @Override
    protected void recoverResources() {
        //TODO
    }
    @Override
    protected void releaseResources() {
        //TODO
    }
    @Override
    protected void updateViews() {
        //TODO

        int bars;
        int lastPage = (bars = mPattern.getNumberOfBars()) / mBarsPerPage - 1;
        if ((bars % mBarsPerPage) != 0) {
            lastPage++;
        }

        mParentButton.setEnabled(!mPattern.mParent.isRoot());
        mRandomiseButton.setEnabled(mPattern.mStepValues.size() > 0);
        mPreviousButton.setEnabled((mCurrentPage > 0));
        mNextButton.setEnabled((mCurrentPage < lastPage));
        mStepsPerBarButton.setEnabled(mPattern.mChildren.size() < 1);
        mBarsButton.setEnabled(mPattern.mChildren.size() < 1);

        //steps------------------------------------------------------------------
        int stepsPerBar = mPattern.getStepsPerBar();
        int firstStepIndex = mCurrentPage * mBarsPerPage * stepsPerBar;
        int lastStepIndex = (mCurrentPage + 1) * mBarsPerPage * stepsPerBar - 1;
        int numberOfSteps;
        if (lastStepIndex >= (numberOfSteps = mPattern.getNumberOfSteps())) {
            lastStepIndex = numberOfSteps - 1;
        }

        int shadesSize = (int) Math.sqrt(stepsPerBar) - 1;
        if (shadesSize < 2) {
            shadesSize = 2;
        }
        int shadeStep = 256 / (shadesSize - 1);
        int shades[] = new int[shadesSize];
        int value = 255;
        for (int i = 0; i < shadesSize; i++) {
            shades[i] = value;
            value -= shadeStep;
            if (value < 0) {
                value = 0;
            }
        }

        mStepsLayout.removeAllViews();

        for (int i = firstStepIndex; i <= lastStepIndex; i++) {
            RealStep step = (RealStep) mPattern.getStep(i);

            int index = shadesSize - 1;
            for (int divider = (stepsPerBar / 4);
                 ((divider > 0)&&((i % divider) != 0)); divider /= 2) {
                index--;
            }

            StepButton button =
                    new StepButton(this, mPattern, step, i, shadesSize, stepsPerBar, shades);
            button.setLayoutParams(mHorizontalLayoutParams);
            step.mButton = button;
            mStepsLayout.addView(button);
        }

        //steps------------------------------------------------------------------

        int pages = (int) Math.ceil(((double) mPattern.getNumberOfBars())
                / ((double) mBarsPerPage));
        setTitle(mPattern.mName + " (" +
                (mCurrentPage + 1) + "/" + pages + ")");
    }
    @Override
    protected int getLayout() {
        return R.layout.activity_main;
    }
    @Override
    protected void initialise() {
        //TODO
    }
    @Override
    protected void uninitialise() {
        //TODO
    }
    @Override
    protected void onServiceUnbound() {
        //TODO
    }
    @Override
    protected void onServiceBound() {
        //TODO
    }
    @Override
    protected Class<?> getServiceToStart() {
        //TODO
        return null;
    }
    @Override
    protected Class<?> getServiceToBind() {
        //TODO
        return null;
    }
    @Override
    protected int getOptionsMenu() {
        //TODO
        return -1;
    }
    @Override
    protected void setOptionItemExecutors() {
        //TODO
    }
    @Override
    protected void setContextItemExecutors() {
        //TODO
    }
    @Override
    protected void registerForContextMenus() {
        //TODO
    }
    @Override
    protected void prepareContextMenu(ContextMenu menu, int position) {

    }
    @Override
    protected void assignHandlers() {
        mBarsButton.setOnClickListener(mOnClickListener);
        mStepsPerBarButton.setOnClickListener(mOnClickListener);
        mBarsPerPageButton.setOnClickListener(mOnClickListener);
        mValuesButton.setOnClickListener(mOnClickListener);
        mRandomiseButton.setOnClickListener(mOnClickListener);
        mPreviousButton.setOnClickListener(mOnClickListener);
        mNextButton.setOnClickListener(mOnClickListener);
        mParentButton.setOnClickListener(mOnClickListener);
        mChildrenButton.setOnClickListener(mOnClickListener);
        mSiblingsButton.setOnClickListener(mOnClickListener);
        mSaveButton.setOnClickListener(mOnClickListener);
        mSubmitButton.setOnClickListener(mOnClickListener);
        mServerButton.setOnClickListener(mOnClickListener);
        mTopButton.setOnClickListener(mOnClickListener);
    }
    @Override
    protected void findElements() {
        mBarsButton = (Button) findViewById(R.id.button_bars);
        mStepsPerBarButton = (Button) findViewById(R.id.button_steps_per_bar);
        mBarsPerPageButton = (Button) findViewById(R.id.button_bars_per_page);
        mValuesButton = (Button) findViewById(R.id.button_values);
        mRandomiseButton = (Button) findViewById(R.id.button_randomise);
        mPreviousButton = (Button) findViewById(R.id.button_previous);
        mNextButton = (Button) findViewById(R.id.button_next);
        mParentButton = (Button) findViewById(R.id.button_parent);
        mChildrenButton = (Button) findViewById(R.id.button_children);
        mSiblingsButton = (Button) findViewById(R.id.button_siblings);
        mSaveButton = (Button) findViewById(R.id.button_save);
        mSubmitButton = (Button) findViewById(R.id.button_submit);
        mServerButton = (Button) findViewById(R.id.button_server);
        mTopButton = (Button) findViewById(R.id.button_top);
        mStepsLayout = (LinearLayout) findViewById(R.id.layout_steps);

        mHaveElements = true;
    }


    //----------------------MainActivity specific methods
    private EmptyExecutor getPatternAddedExecutor() {
        if (mPatternAddedExecutor == null) {
            mPatternAddedExecutor =
                    new PatternAddedExecutor(mPatternListDialog.getPatternListAdapter(), this);
        }
        return mPatternAddedExecutor;
    }
    private void unsetStep(Pattern p, RealStep s) {
        s.mStepValue = null;
        for (RealPattern pattern : p.mChildren) {
            for (Bar bar : pattern.mBars) {
                RealBar realBar = (RealBar) bar;
                for (Step step : realBar.mSteps) {
                    RealStep realStep = (RealStep) step;
                    if (realStep.mParent == s) {
                        unsetStep(pattern, realStep);
                    }
                }
            }
        }

    }
    private void setupPattern(RealPattern pattern, String name,
                              int channel, boolean isNote, int controller) {
        if (isNote) {
            pattern.setup(name, channel);
        } else {
            pattern.setup(name, channel, controller);
        }
        if (pattern == mPattern) {
            updateViews();
        }
    }
    private void setBars(int bars) {
        MainActivity.this.mPattern.setNumberOfBars(bars);
        mCurrentPage = 0;
        updateViews();
    }
    private void setStepsPerBar(int stepsPerBar) {
        mPattern.setStepsPerBar(stepsPerBar);
        updateViews();
    }
    private void onClick(View view) {
        if (view == mBarsButton) {
            handleOnBars();
        } else if (view == mStepsPerBarButton) {
            handleOnStepsPerBar();
        } else if (view == mBarsPerPageButton) {
            handleOnBarsPerPage();
        } else if (view == mValuesButton) {
            handleOnValues();
        } else if (view == mRandomiseButton) {
            handleOnRandomise();
        } else if (view == mPreviousButton) {
            handleOnPrevious();
        } else if (view == mNextButton) {
            handleOnNext();
        } else if (view == mParentButton) {
            handleOnParent();
        } else if (view == mChildrenButton) {
            handleOnChildren();
        } else if (view == mSiblingsButton) {
            handleOnSiblings();
        } else if (view == mSaveButton) {
            handleOnSave();
        } else if (view == mSubmitButton) {
            handleOnSubmit();
        } else if (view == mServerButton) {
            handleOnServer();
        } else if (view == mTopButton) {
            handleOnTop();
        }
    }
    private void handleOnTop() {
        String makeTopLevel = getResources().getString(R.string.make_top_level);
        String yes = getResources().getString(R.string.yes);
        String no = getResources().getString(R.string.no);
        askYesNo(makeTopLevel, yes, no, new YesNoable(null) {
            @Override
            public void yes() {
                mPattern.changeParent(mRootPattern);
                updateViews();
            }

            @Override
            public void no() {

            }
        });
    }
    private void handleOnServer() {
        ServerDialog serverDialog = getServerDialog();
        serverDialog.reset(getServerString());
        showDialog(serverDialog);
    }
    private ServerDialog getServerDialog() {
        if (mServerDialog == null) {
            mServerDialog = new ServerDialog();
        }
        return mServerDialog;
    }
    private String getServerString() {
        return PreferenceManager
                .getDefaultSharedPreferences(this).getString("server", "");
    }
    private NumberPickerDialog getNumberPickerDialog() {
        if (mNumberPickerDialog == null) {
            mNumberPickerDialog =
                    new NumberPickerDialog();
        }
        return mNumberPickerDialog;
    }
    private void setServerFromString(String serverString) throws Throwable {

        String server = getServer(serverString);
        int port = getPort(serverString);
        mServer = server;
        mPort = port;
    }
    private int getPort(String serverString) throws Throwable {
        int colonIndex = getColonIndex(serverString);
        String portString = serverString.substring(colonIndex + 1);
        int result = Integer.parseInt(portString);
        if ((result < 0)||(result >= Math.pow(2, 16))) {
            throw new Throwable("((port < 0)||(port >= Math.pow(2, 16)))");
        }
        return result;
    }
    private String getServer(String serverString) throws Throwable {
        int colonIndex = getColonIndex(serverString);
        return serverString.substring(0, colonIndex);
    }
    private int getColonIndex(String serverString) throws Throwable {
        int result = serverString.indexOf(":");
        if (result < 0) {
            throw new Throwable("(colonIndex < 0)");
        }
        return result;
    }
    private void handleOnBars() {
        mSetBarsNextNumberGetter.setMultiple(mPattern.mParent.getNumberOfBars());
        NumberPickerDialog numberPickerDialog = getNumberPickerDialog();
        numberPickerDialog.reset(mPattern.mBars.size(),
                mSetBarsNextNumberGetter, mSetBarsExecutor);
        showDialog(numberPickerDialog);
    }
    private void handleOnStepsPerBar() {
        int stepsPerBar = mPattern.getStepsPerBar();
        mSetStepsPerBarNextNumberGetter.setMinimum(mPattern.mParent.getStepsPerBar());
        NumberPickerDialog numberPickerDialog = getNumberPickerDialog();
        numberPickerDialog.reset(stepsPerBar,
                mSetStepsPerBarNextNumberGetter, mSetStepsPerBarExecutor);
        showDialog(numberPickerDialog);
    }
    private void handleOnBarsPerPage() {
        NumberPickerDialog numberPickerDialog = getNumberPickerDialog();
        numberPickerDialog.reset(mBarsPerPage,
                mSetBarsPerPageNextNumberGetter, mSetBarsPerPageExecutor);
        showDialog(numberPickerDialog);
    }
    private void handleOnSiblings() {
        Pattern parent = mPattern.mParent;
        handleOnPatternList(parent, parent.mChildren, mPattern);
    }
    private void handleOnChildren() {
        handleOnPatternList(mPattern, mPattern.mChildren, null);
    }
    private void handleOnPatternList(Pattern parent, List<RealPattern> list, Pattern exclude) {
        PatternListDialog patternListDialog = getPatternListDialog();
        NewPatternHandler newPatternHandler = getNewPatternHandler();
        newPatternHandler.reset(getResources().getString(R.string.untitled),
                parent);
        patternListDialog.reset(list, exclude, newPatternHandler);
        showDialog(patternListDialog);
    }
    private NewPatternHandler getNewPatternHandler() {
        if (mNewPatternHandler == null) {
            mNewPatternHandler = new NewPatternHandler();
        }
        return mNewPatternHandler;
    }
    private PatternListDialog getPatternListDialog() {
        if (mPatternListDialog == null) {
            mPatternListDialog = new PatternListDialog();
        }
        return mPatternListDialog;
    }
    private void handleOnValues() {
        StepValuesDialog stepValuesDialog = getStepValuesDialog();
        stepValuesDialog.reset(mPattern);
        showDialog(stepValuesDialog);
    }
    private void handleOnRandomise() {
        RandomiseDialog randomiseDialog = getRandomiseDialog();
        randomiseDialog.reset(mPattern);
        showDialog(randomiseDialog);
    }
    private RandomiseDialog getRandomiseDialog() {
        if (mRandomiseDialog == null) {
            mRandomiseDialog = new RandomiseDialog();
        }
        return mRandomiseDialog;
    }
    private void handleOnPrevious() {
        mCurrentPage--;
        updateViews();
    }
    private void handleOnNext() {
        mCurrentPage++;
        updateViews();
    }
    private void handleOnParent() {
        changePattern((RealPattern) mPattern.mParent);
    }
    private void changePattern(RealPattern parent) {
        enterPattern(parent);
        updateViews();
    }
    private void handleOnSave() {
        //TODO
    }
    private HTTPServerRequestWorker mHTTPServerRequestWorker =
            new HTTPServerRequestWorker();
    private void handleOnSubmit() {
        submit();
    }
    private void submit() {
        try {
            JSONObject config = getConfig();
            mHTTPServerRequestWorker.reset(mServer, mPort, config);

            HTTPServerRequest httpServerRequest = new HTTPServerRequest(this,
                    null, null,
                    mHTTPServerRequestWorker, mFailedRunnable, null,
                    R.string.ok, R.string.cancel, R.string.server_certificate);
            httpServerRequest.execute();
        } catch (Throwable throwable) {
            Toast.makeText(this, throwable.toString(), Toast.LENGTH_SHORT).show();
            throwable.printStackTrace();
        }
    }
    private JSONObject getConfig() throws Throwable {
        JSONObject result = new JSONObject();
        int bars = getBars(mRootPattern);
        int stepsPerBar = getHighestStepsPerBar(mRootPattern);
        int lastStep = stepsPerBar * bars - 1;
        JSONArray stepsArray = new JSONArray();

        for (int i = 0; i <= lastStep; i++) {
            JSONObject step = new JSONObject();
            JSONArray events = new JSONArray();

            step.put("barDivider", stepsPerBar);
            step.put("position", i);


            addEvents(events, mRootPattern, i, stepsPerBar, lastStep);
            step.put("events", events);

            stepsArray.put(step);
        }

        result.put("bars", bars);
        result.put("steps", stepsArray);
        return result;
    }
    private void addEvents(JSONArray events, Pattern pattern, int atStep,
                           int stepsPerBar, int lastStep) throws Throwable {
        if (!pattern.isRoot()) {
            addEvent(events, (RealPattern) pattern, atStep, stepsPerBar, lastStep);
        }
        for (Pattern p : pattern.mChildren) {
            addEvents(events, p, atStep, stepsPerBar, lastStep);
        }
    }
    private void addEvent(JSONArray events, RealPattern pattern,
                          int atStep, int stepsPerBar, int lastStep) throws Throwable {
        int multiplier = stepsPerBar / pattern.getStepsPerBar();
        if ((atStep % multiplier) == 0) {
            atStep /= multiplier;
            JSONObject event = doAddEvent(events, pattern, atStep, (lastStep / multiplier));
            if (event != null) {
                events.put(event);
            }
        }
    }
    private JSONObject doAddEvent(JSONArray events, RealPattern pattern,
                                  int atStep, int lastStep) throws Throwable {
        JSONObject result = null;
        int patternSteps = pattern.mBars.size() * pattern.getStepsPerBar();
        int atPatternStep = atStep % patternSteps;
        RealStep step = (RealStep) pattern.getStep(atPatternStep);

        if (step.isSet()) {
            if (pattern.isNote()) {
                result = addNoteEvent(pattern, patternSteps,
                        step, atStep, lastStep);
            } else {
                result = addControllerEvent(pattern, step);
            }
        }

        return result;
    }
    private JSONObject addNoteEvent(RealPattern pattern,
                                    int patternSteps, RealStep step, int atStep,
                                    int lastStep) throws Throwable {
        JSONObject result = null;
        boolean doAdd = true;
        NoteStepValue stepValue = (NoteStepValue) step.mStepValue;

        if (atStep > 0) {
            int previousAtStep = atStep - 1;
            int previousAtPatternStep = previousAtStep % patternSteps;

            RealStep previousStep = (RealStep) pattern.getStep(previousAtPatternStep);

            if (previousStep.isSet()) {
                NoteStepValue previousStepValue = (NoteStepValue) previousStep.mStepValue;

                if ((previousStepValue.mOctave == stepValue.mOctave)
                        && (previousStepValue.mNotee == stepValue.mNotee)
                        && (previousStepValue.mSharp == stepValue.mSharp)
                        &&(previousStep.mSlide)) {
                    doAdd = false;
                }
            }
        }

        if (doAdd) {
            result = doAddNoteEvent(step, atStep, lastStep, patternSteps, pattern, stepValue);
        }

        return result;
    }
    private JSONObject doAddNoteEvent(RealStep step, int atStep, int lastStep, int patternSteps,
                                      RealPattern pattern, NoteStepValue stepValue)
            throws JSONException {
        JSONObject result = new JSONObject();
        int length = 0;

        if (!step.mSlide) {
            length = 2;
        } else {
            while (true) {
                length += 4;
                if (atStep == lastStep) {
                    break;
                }
                atStep++;
                int atPatternStep = atStep % patternSteps;
                step = (RealStep) pattern.getStep(atPatternStep);
                if (!step.isSet()) {
                    break;
                }
                NoteStepValue sV = (NoteStepValue) step.mStepValue;
                if ((sV.mOctave != stepValue.mOctave)
                        || (sV.mNotee != stepValue.mNotee)
                        || (sV.mSharp != stepValue.mSharp)) {
                    length++;
                    break;
                } else if (!step.mSlide) {
                    length += 2;
                    break;
                }
            }
        }

        result.put("type", "note");
        JSONObject note = new JSONObject();
        note.put("channel", pattern.mChannel);
        note.put("note", noteToInteger(stepValue.mOctave,
                stepValue.mNotee, stepValue.mSharp));
        note.put("velocity", 127);
        note.put("lengthDivider", pattern.getStepsPerBar() * 4);
        note.put("length", length);
        result.put("note", note);

        return result;
    }
    private int noteToInteger(int octave, int note, boolean sharp) {
        int result = C0;
        result += (octave * 12);
        result += NoteStepValue.NOTE_OFFSET[note];
        if ((NoteStepValue.NOTE_MAY_BE_SHARP[note]) && sharp) {
            result++;
        }
        return result;
    }
    private JSONObject addControllerEvent(RealPattern pattern,
                                    RealStep step) throws Throwable {
        JSONObject result = new JSONObject();

        ControllerStepValue stepValue = (ControllerStepValue) step.mStepValue;
        result.put("type", "controller");
        JSONObject controller = new JSONObject();
        controller.put("channel", pattern.mChannel);
        controller.put("parameter", pattern.mController);
        controller.put("value", stepValue.mValue);
        result.put("controller", controller);

        return result;
    }
    private int getHighestStepsPerBar(Pattern pattern) {
        int result = pattern.getStepsPerBar();

        for (Pattern p : pattern.mChildren) {
            int highestStepsPerBar = getHighestStepsPerBar(p);
            if (highestStepsPerBar > result) {
                result = highestStepsPerBar;
            }
        }

        return result;
    }
    private int getBars(Pattern pattern) {
        int result = pattern.mBars.size();
        for (Pattern p : pattern.mChildren) {
            result = leastCommonMultiple(result, getBars(p));
        }
        return result;
    }
    private int leastCommonMultiple(int a, int b) {
        int result = -1;
        result = a * (b / greatestCommonDivisor(a, b));
        return result;
    }
    private int greatestCommonDivisor(int result, int b) {
        while (b > 0)
        {
            int temp = b;
            b = result % b;
            result = temp;
        }
        return result;
    }
    private SetupPatternDialog getSetupPatternDialog() {
        if (mSetupPatternDialog == null) {
            mSetupPatternDialog = new SetupPatternDialog();
        }
        return mSetupPatternDialog;
    }
    private Handler getHandler() {
        if (mHandler == null) {
            mHandler = new Handler();
        }
        return mHandler;
    }
    private void enterPattern(RealPattern pattern) {
        mPattern = pattern;
    }
    private void setBarsPerPage(int barsPerPage) {
        mCurrentPage = (int) ((((double) mBarsPerPage) /
                ((double) barsPerPage)) * ((double) mCurrentPage));
        mBarsPerPage = barsPerPage;
        if (mHaveElements) {
            updateViews();
        }
    }
    private RealPattern readPattern(Pattern parent, String filename) {
        //TODO
        return null;
    }
    private void testServer(String server, int port) {
        mHTTPServerRequestWorker.reset(server, port, null);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, R.string.ok, Toast.LENGTH_SHORT).show();
            }
        };
        HTTPServerRequest httpServerRequest = new HTTPServerRequest(this,
                runnable, null,
                mHTTPServerRequestWorker, mFailedRunnable, null,
                R.string.ok, R.string.cancel, R.string.server_certificate);
        httpServerRequest.execute();
    }
    private void setServer(String server) {
        SharedPreferences.Editor editor = PreferenceManager
                .getDefaultSharedPreferences(this).edit();
        editor.putString("server", server);
        editor.commit();

        try {
            setServerFromString(getServerString());
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
    private void randomise(RealPattern pattern, int probabilityPercentage,
                           int slideProbabilityPercentage,
                           List<RandomiseDialog.OffByCheckBox> offByCheckBoxes) {
        StepValue lastStepValue = null;
        boolean sliding = false;
        for (Bar bar : this.mPattern.mBars) {
            for (Step step : bar.mSteps) {
                RealStep realStep = (RealStep) step;
                realStep.mStepValue = null;
                if ((!realStep.isSettable())||
                        (!offByCheckBoxes.get(realStep.mOffsetFromParent).isChecked())) {
                    sliding = false;
                    continue;
                }
                realStep.mStepValue =
                        getRandomStepValue(probabilityPercentage,
                                this.mPattern.mStepValues, lastStepValue, sliding);
                if (realStep.mStepValue == null) {
                    continue;
                }
                lastStepValue = realStep.mStepValue;
                if (pattern.isNote()) {
                    realStep.mSlide = sliding = getRamdomSlide(slideProbabilityPercentage);
                }
            }
        }

        updateViews();
    }
    private boolean getRamdomSlide(int slideProbabilityPercentage) {
        return getRandomBoolean(slideProbabilityPercentage);
    }
    private boolean getRandomBoolean(int probabilityPercentage) {
        Random random = getRandom();
        int invertedProbability = 100 - probabilityPercentage;
        return ((random.nextInt(100) - invertedProbability) >= 0);
    }
    private Random getRandom() {
        if (mRandom == null) {
            mRandom = new Random();
        }
        return mRandom;
    }
    private StepValue getRandomStepValue(int probabilityPercentage, List<StepValue> stepValues,
                                         StepValue lastStepValue, boolean sliding) {
        StepValue result = null;

        if (getRandomBoolean(probabilityPercentage)) {
            result = doGetRandomStepValue(stepValues, lastStepValue, sliding);
        }
        return result;
    }
    private StepValue doGetRandomStepValue(List<StepValue> stepValues,
                                           StepValue lastStepValue, boolean sliding) {
        StepValue result = null;
        int length = stepValues.size();
        do {
            result = stepValues.get(getRandom().nextInt(length));
        } while ((!sliding)&&(length > 1)&&(result == lastStepValue));

        return result;
    }


    //----------------------MainActivity inner classes
    private class HTTPServerRequestWorker implements HTTPServerRequest.Worker {

        private JSONObject mConfig;
        private List<BasicNameValuePair> mTestHeaders;
        private List<BasicNameValuePair> mLiveHeaders;
        private int mPort;
        private String mServer;

        @Override
        public AsyncTaskResult work() {
            AsyncTaskResult result = new AsyncTaskResult();
            InputStream inputStream = null;
            String fingerprint = null;
            Throwable throwable = null;
            List<BasicNameValuePair> headers;

            if (mConfig == null) {
                headers = getTestHeaders();
            } else {
                headers = getLiveHeaders();
            }

            try {
                StringEntity entity = null;
                if (mConfig != null) {
                    entity = new StringEntity(mConfig.toString());
                }
                HttpResponse httpResponse =
                        HTTP.doHttp(this.mServer, this.mPort, "ksequencer", null, null,
                                headers, null, entity, org.kohaerenzstiftung.HTTP.HTTP_PUT);
                int code = httpResponse.getStatusLine().getStatusCode();
                if (code != HttpStatus.SC_OK) {
                    throw new Exception("HTTP Status: " + code);
                }
            } catch (Throwable t) {
                throwable = t;
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                    }
                }
            }
            result.setThrowable(throwable);
            result.setFingerprint(fingerprint);

            return result;
        }

        private List<BasicNameValuePair> getTestHeaders() {
            if (mTestHeaders == null) {
                mTestHeaders = createHeaders("test");
            }

            return mTestHeaders;
        }

        private List<BasicNameValuePair> getLiveHeaders() {
            if (mLiveHeaders == null) {
                mLiveHeaders = createHeaders("live");
            }

            return mLiveHeaders;
        }

        private List<BasicNameValuePair> createHeaders(String action) {
            LinkedList<BasicNameValuePair> result =
                    new LinkedList<BasicNameValuePair>();
            result.add(new BasicNameValuePair("action", action));
            return result;
        }

        public void reset(String server, int port, JSONObject config) {
            this.mServer = server;
            this.mPort = port;
            mConfig = config;
        }
    }
    private class NewPatternHandler {
        private String mName;
        private Pattern mParent;

        RealPattern getNewPattern() {
            return new RealPattern(mName, mParent);
        }

        private void reset(String name, Pattern parent) {
            mName = name;
            mParent = parent;
        }
    }
    class StepButton extends LinearLayout {

        private final ViewGroup.LayoutParams mLayoutParams = new
                LayoutParams(LayoutParams.MATCH_PARENT, 0, 1);
        private final TextView mUpperTextView;
        private final Context mContext;
        private final TextView mLowerTextView;
        private final RealPattern mPattern;
        private Button mSlideButton = null;
        private RealStep mStep = null;
        private final Button mOnOffButton;
        private final Button mNextValueButton;

        private Button getButton() {
            Button result = new Button(mContext);
            result.setLayoutParams(mLayoutParams);
            return result;
        }

        private StepButton(Context context,
                           RealPattern pattern,
                           RealStep step,
                           int i, int shadesSize, int stepsPerBar,
                           int[] shades) {
            super(context);

            mPattern = pattern;
            mStep = step;
            mContext = context;

            setOrientation(VERTICAL);

            mUpperTextView = getTextView(i, shadesSize, stepsPerBar, shades);
            addView(mUpperTextView);

            mOnOffButton = getButton();
            mOnOffButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mStep.mStepValue != null) {
                        MainActivity.this.unsetStep(StepButton.this.mPattern, StepButton.this.mStep);
                        mStep.mStepValue = null;
                    } else {
                        mStep.mStepValue = mPattern.mStepValues.get(0);
                    }
                    updateViews();
                }
            });
            addView(mOnOffButton);
            mLowerTextView = getTextView();
            addView(mLowerTextView);
            mNextValueButton = getButton();
            mNextValueButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    int index = mPattern.mStepValues.indexOf(mStep.mStepValue);
                    index++;
                    if (index >= mPattern.mStepValues.size()) {
                        index = 0;
                    }
                    mStep.mStepValue = mPattern.mStepValues.get(index);
                    updateViews();
                }
            });
            addView(mNextValueButton);
            if (step.mPattern.isNote()) {
                mSlideButton = getButton();
                mSlideButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mStep.mSlide = !mStep.mSlide;
                        updateViews();
                    }
                });
                addView(mSlideButton);
            }
            updateViews();
        }

        private void updateViews() {
            boolean slideButtonEnabled = false;
            String slideButtonText = "-";
            boolean onOffButtonEnabled = true;
            String onOffButtonText = "-";
            boolean nextValueButtonEnabled = false;
            String nextValueButtonText = "-";
            String lowerTextViewText = "-";

            boolean activated = mStep.isSet();
            boolean activable =
                    mStep.isSettable() && (mStep.mPattern.mStepValues.size() > 0);


            if (activable) {
                onOffButtonText = getResources().getString(R.string.on);
            } else if (activated) {
                lowerTextViewText = mStep.mStepValue.getRepresentation();
                onOffButtonText = getResources().getString(R.string.off);
                nextValueButtonEnabled = (mStep.mPattern.mStepValues.size() > 1);
                if (nextValueButtonEnabled) {
                    nextValueButtonText = getResources().getString(R.string.next);
                }
                slideButtonEnabled = true;
                if (mStep.mSlide) {
                    slideButtonText = getResources().getString(R.string.unslide);
                } else {
                    slideButtonText = getResources().getString(R.string.slide);
                }
            } else {
                onOffButtonEnabled = false;
            }

            mOnOffButton.setEnabled(onOffButtonEnabled);
            mNextValueButton.setEnabled(nextValueButtonEnabled);

            mOnOffButton.setText(onOffButtonText);
            mNextValueButton.setText(nextValueButtonText);
            mLowerTextView.setText(lowerTextViewText);

            if (mPattern.isNote()) {
                mSlideButton.setEnabled(slideButtonEnabled);
                mSlideButton.setText(slideButtonText);
            }
        }

        private TextView getTextView() {
            TextView result = new TextView(mContext);
            result.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);

            result.setLayoutParams(mLayoutParams);

            return result;
        }

        private TextView getTextView(int i, int shadesSize, int stepsPerBar, int[] shades) {
            TextView result = new TextView(mContext);
            result.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
            result.setText(Integer.toString(i + 1));
            int index = shadesSize - 1;
            for (int divider = (stepsPerBar / 4); ((divider > 0)&&((i % divider) != 0)); divider /= 2) {
                index--;
            }
            result.setBackgroundColor(Color.rgb(shades[index], shades[index], shades[index]));
            result.setLayoutParams(mLayoutParams);

            return result;
        }

        private boolean isSet() {
            return mStep.isSet();
        }

        private boolean isSlide() {
            return mStep.isSlide();
        }
    }

    private class SetStepsPerBarNextNumberGetter extends NextNumberGetter {

        private void setMinimum(int minimum) {
            mMinimum = minimum;
        }

        private int mMinimum;

        @Override
        public int getNext(int current, boolean up) {
            int result = mMinimum;
            if (up) {
                result = current * 2;
                if (result < 0) {
                    result = current;
                }
            } else if (current > mMinimum) {
                result = current / 2;
            }
            return result;
        }


    }
    private class SetBarsNextNumberGetter extends NextNumberGetter {
        private void setMultiple(int multiple) {
            this.mMultiple = multiple;
        }

        private int mMultiple = 1;

        @Override
        public int getNext(int current, boolean up) {
            int result = mMultiple;
            if (up) {
                result = current + mMultiple;
                if (result < 0) {
                    result = current;
                }
            } else if (current > mMultiple) {
                result = current - mMultiple;
            }
            return result;
        }
    }

    private abstract class EmptyExecutor {
        abstract void execute();
    }
    private class PatternAddedExecutor extends EmptyExecutor {

        private final PatternListDialog.PatternListAdapter mPatternListAdapter;
        private final MainActivity mMainActivity;

        private PatternAddedExecutor(PatternListDialog.PatternListAdapter patternListAdapter,
                                     MainActivity mainActivity) {
            mPatternListAdapter = patternListAdapter;
            mMainActivity = mainActivity;
        }

        @Override
        void execute() {
            mPatternListAdapter.notifyDataSetChanged();
            mMainActivity.updateViews();
        }
    }
    private abstract class NumberPickerDialogExecutor {
        abstract void execute(int number);
    }

    private class StepValuesDialog extends Dialog {
        private EditNoteStepValueDialog mEditNoteStepValueDialog;
        private EditControllerStepValueDialog mEditControllerStepValueDialog;

        abstract private class EditStepValueDialog extends Dialog {
            protected StepValue mStepValue;
            protected boolean mAdd;

            public EditStepValueDialog(StandardActivity activity, int layout, boolean cancelable) {
                super(activity, layout, cancelable);
            }

            void reset(StepValue stepValue, boolean add) {
                mStepValue = stepValue;
                mAdd = add;
            }
        }

        private class EditNoteStepValueDialog extends EditStepValueDialog {
            private RadioButton mCRadioButton;
            private RadioButton mDRadioButton;
            private RadioButton mERadioButton;
            private RadioButton mFRadioButton;
            private RadioButton mGRadioButton;
            private RadioButton mARadioButton;
            private RadioButton mBRadioButton;
            private CheckBox mSharpCheckBox;
            private EditText mOctaveEditText;
            private Button mOkButton;
            private RadioGroup mNoteRadioGroup;
            private int mUpdatingViews = 0;

            EditNoteStepValueDialog() {
                super(MainActivity.this, R.layout.dialog_notestepvalue, false);
            }

            @Override
            protected void prepareContextMenu(ContextMenu menu, int position) {

            }

            @Override
            protected void setContextItemExecutors() {

            }

            @Override
            protected void registerForContextMenus() {

            }

            @Override
            protected void updateViews() {
                mUpdatingViews++;
                NoteStepValue stepValue = (NoteStepValue) mStepValue;
                mSharpCheckBox.setChecked(stepValue.mSharp);
                mOctaveEditText.setText("" + stepValue.mOctave);
                mSharpCheckBox.setEnabled(true);
                switch (stepValue.mNotee) {
                    case NoteStepValue.NOTE_C: mCRadioButton.setChecked(true); break;
                    case NoteStepValue.NOTE_D: mDRadioButton.setChecked(true); break;
                    case NoteStepValue.NOTE_E: mERadioButton.setChecked(true); break;
                    case NoteStepValue.NOTE_F: mFRadioButton.setChecked(true); break;
                    case NoteStepValue.NOTE_G: mGRadioButton.setChecked(true); break;
                    case NoteStepValue.NOTE_A: mARadioButton.setChecked(true); break;
                    case NoteStepValue.NOTE_B: mBRadioButton.setChecked(true); break;
                }
                refresh(false);
                mUpdatingViews--;
            }

            @Override
            protected void recoverResources() {

            }

            @Override
            protected void releaseResources() {

            }

            @Override
            protected void findElements() {
                mNoteRadioGroup = (RadioGroup) findViewById(R.id.radiogroup_note);
                mCRadioButton = (RadioButton) findViewById(R.id.radiobutton_c);
                mDRadioButton = (RadioButton) findViewById(R.id.radiobutton_d);
                mERadioButton = (RadioButton) findViewById(R.id.radiobutton_e);
                mFRadioButton = (RadioButton) findViewById(R.id.radiobutton_f);
                mGRadioButton = (RadioButton) findViewById(R.id.radiobutton_g);
                mARadioButton = (RadioButton) findViewById(R.id.radiobutton_a);
                mBRadioButton = (RadioButton) findViewById(R.id.radiobutton_b);
                mSharpCheckBox = (CheckBox) findViewById(R.id.checkbox_sharp);
                mOctaveEditText = (EditText) findViewById(R.id.edittext_octave);
                mOkButton = (Button) findViewById(R.id.button_ok);

            }

            @Override
            protected void assignHandlers() {
                mNoteRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        EditNoteStepValueDialog.this.refresh(true);
                    }
                });
                mSharpCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        EditNoteStepValueDialog.this.refresh(true);
                    }
                });
                mOctaveEditText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        EditNoteStepValueDialog.this.refresh(true);
                    }
                });
                mOkButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mAdd) {
                            mPattern.mStepValues.add(mStepValue);
                        }
                        getStepValuesAdapter().notifyDataSetChanged();
                        MainActivity.this.updateViews();
                        dismiss();
                    }
                });

            }

            private void refresh(boolean modify) {
                if (mUpdatingViews > 0) {
                    return;
                }

                boolean okButtonEnabled = true;
                int octave = 0;
                try {
                    octave = Integer.parseInt(mOctaveEditText.getText().toString());
                } catch (Throwable t) {
                    okButtonEnabled = false;
                }
                boolean sharpCheckBoxEnabled = true;
                int note = NoteStepValue.NOTE_C;
                if (false);
                else if (mCRadioButton.isChecked()) note = NoteStepValue.NOTE_C;
                else if (mDRadioButton.isChecked()) note = NoteStepValue.NOTE_D;
                else if (mERadioButton.isChecked()) note = NoteStepValue.NOTE_E;
                else if (mFRadioButton.isChecked()) note = NoteStepValue.NOTE_F;
                else if (mGRadioButton.isChecked()) note = NoteStepValue.NOTE_G;
                else if (mARadioButton.isChecked()) note = NoteStepValue.NOTE_A;
                else if (mBRadioButton.isChecked()) note = NoteStepValue.NOTE_B;

                sharpCheckBoxEnabled = NoteStepValue.NOTE_MAY_BE_SHARP[note];

                mSharpCheckBox.setEnabled(sharpCheckBoxEnabled);
                if (!sharpCheckBoxEnabled) {
                    mSharpCheckBox.setChecked(false);
                }
                boolean sharp = mSharpCheckBox.isChecked();
                mOkButton.setEnabled(okButtonEnabled);
                if (modify) {
                    NoteStepValue stepValue = (NoteStepValue) mStepValue;
                    stepValue.mOctave = octave;
                    stepValue.mNotee = note;
                    stepValue.mSharp = sharp;
                }
            }
        }

        private class EditControllerStepValueDialog extends EditStepValueDialog {
            private EditText mValueEditText;
            private Button mOkButton;

            EditControllerStepValueDialog() {
                super(MainActivity.this, R.layout.dialog_controllerstepvalue, false);
            }

            @Override
            protected void prepareContextMenu(ContextMenu menu, int position) {

            }

            @Override
            protected void setContextItemExecutors() {

            }

            @Override
            protected void registerForContextMenus() {

            }

            @Override
            protected void updateViews() {
                ControllerStepValue stepValue = (ControllerStepValue) mStepValue;
                mValueEditText.setText(stepValue.mValue + "");
            }

            @Override
            protected void recoverResources() {

            }

            @Override
            protected void releaseResources() {

            }

            @Override
            protected void findElements() {
                mValueEditText = (EditText) findViewById(R.id.edittext_value);
                mOkButton = (Button) findViewById(R.id.button_ok);

            }

            @Override
            protected void assignHandlers() {
                mValueEditText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        mOkButton.setEnabled(false);
                        try {
                            int value = Integer.parseInt(mValueEditText.getText().toString());
                            if (value >= 0) {
                                mOkButton.setEnabled(true);
                                ControllerStepValue stepValue = (ControllerStepValue) mStepValue;
                                stepValue.mValue = value;
                            }
                        } catch (Throwable t) {
                        }
                    }
                });
                mOkButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mAdd) {
                            mPattern.mStepValues.add(mStepValue);
                        }
                        getStepValuesAdapter().notifyDataSetChanged();
                        MainActivity.this.updateViews();
                        dismiss();
                    }
                });
            }
        }

        private StepValuesAdapter mStepValuesAdapter;

        private class StepValuesAdapter extends BaseAdapter {
            @Override
            public int getCount() {
                return mPattern.mStepValues.size();
            }

            @Override
            public Object getItem(int position) {
                return mPattern.mStepValues.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                android.widget.TextView result = new android.widget.TextView(MainActivity.this);
                StepValue stepValue = (StepValue) getItem(position);
                result.setText(stepValue.getRepresentation());
                return result;
            }
        }

        private RealPattern mPattern;
        private ListView mStepValuesListView;
        private Button mAddButton;

        StepValuesDialog() {
            super(MainActivity.this, R.layout.dialog_stepvalues, true);
        }

        @Override
        protected void prepareContextMenu(ContextMenu menu, int position) {
            StepValue stepValue = (StepValue) getStepValuesAdapter().getItem(position);
            boolean delete = true;
            for (Bar bar : mPattern.mBars) {
                for (Step step : bar.mSteps) {
                    RealStep realStep = (RealStep) step;
                    if (realStep.mStepValue == stepValue) {
                        delete = false;
                        break;
                    }
                }
            }
            if (!delete) {
                menu.removeItem(R.id.menuitem_deletestepvalue);
            }
        }

        @Override
        protected void setContextItemExecutors() {
            setContextItemExecutor(R.id.menuitem_editstepvalue, new ContextItemExecutor() {
                public void execute(MenuItem item) {
                    AdapterView.AdapterContextMenuInfo menuInfo =
                            (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                    StepValue stepValue = mPattern.mStepValues.get((int) menuInfo.id);
                    editStepValue(stepValue);
                    getStepValuesAdapter().notifyDataSetChanged();
                }
            });
            setContextItemExecutor(R.id.menuitem_deletestepvalue, new ContextItemExecutor() {
                public void execute(MenuItem item) {
                    AdapterView.AdapterContextMenuInfo menuInfo =
                            (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                    StepValue stepValue = mPattern.mStepValues.remove((int) menuInfo.id);
                    getStepValuesAdapter().notifyDataSetChanged();
                    MainActivity.this.updateViews();
                }
            });
        }

        private void editStepValue(StepValue stepValue) {
            EditStepValueDialog dialog;
            if (mPattern.isNote()) {
                dialog = getEditNoteStepValueDialog();
            } else {
                dialog = getEditControllerStepValueDialog();
            }
            boolean add = false;
            if (stepValue == null) {
                add = true;
                stepValue = mPattern.isNote() ? new NoteStepValue() :
                        new ControllerStepValue();
            }
            dialog.reset(stepValue, add);
            showDialog(dialog);
        }

        private EditNoteStepValueDialog getEditNoteStepValueDialog() {
            if (mEditNoteStepValueDialog == null) {
                mEditNoteStepValueDialog = new EditNoteStepValueDialog();
            }
            return mEditNoteStepValueDialog;
        }

        private EditControllerStepValueDialog getEditControllerStepValueDialog() {
            if (mEditControllerStepValueDialog == null) {
                mEditControllerStepValueDialog = new EditControllerStepValueDialog();
            }
            return mEditControllerStepValueDialog;
        }

        @Override
        protected void registerForContextMenus() {
            //TODO: delete only for unused step values!!!!!!!!!!!!
            registerForContextMenu(mStepValuesListView, new ContextMenuCreator() {
                public int createContextMenu(ContextMenu.ContextMenuInfo menuInfo) {
                    return R.menu.context_stepvalues;
                }
            });
        }

        @Override
        protected void updateViews() {
            StepValuesAdapter stepValuesAdapter =
                    getStepValuesAdapter();
            mStepValuesListView.setAdapter(stepValuesAdapter);
        }

        private StepValuesAdapter getStepValuesAdapter() {
            if (mStepValuesAdapter == null) {
                mStepValuesAdapter = new StepValuesAdapter();
            }
            return mStepValuesAdapter;
        }

        @Override
        protected void recoverResources() {

        }

        @Override
        protected void releaseResources() {

        }

        @Override
        protected void findElements() {
            mStepValuesListView = (ListView) findViewById(R.id.listview_stepvalues);
            mAddButton = (Button) findViewById(R.id.button_add);
        }

        @Override
        protected void assignHandlers() {
            mAddButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleAddStepValue();
                }
            });
        }

        private void handleAddStepValue() {
            editStepValue(null);
        }

        void reset(RealPattern pattern) {
            mPattern = pattern;
        }
    }
    private class SetupPatternDialog extends Dialog {
        private EditText mNameEditText;
        private Button mOkButton;
        private RealPattern mPattern;
        private EmptyExecutor mEmptyExecutor;
        private EditText mChannelEditText;
        private RadioButton mNoteRadioButton;
        private RadioButton mControllerRadioButton;
        private EditText mControllerEditText;
        private RadioGroup mTypeRadioGroup;
        private RadioGroup.OnCheckedChangeListener mOnCheckedChangedListener;
        private TextWatcher mTextWatcher;


        private SetupPatternDialog() {
            super(MainActivity.this, R.layout.dialog_setuppattern, false);
        }

        @Override
        protected void prepareContextMenu(ContextMenu menu, int position) {

        }

        @Override
        protected void setContextItemExecutors() {

        }

        @Override
        protected void registerForContextMenus() {

        }

        @Override
        protected void updateViews() {
            boolean enabled = false;

            mControllerEditText.setEnabled(!mNoteRadioButton.isChecked());
            String name = mNameEditText.getText().toString().trim();

            if (!name.equals("")) {
                try {
                    int channel = Integer.parseInt(mChannelEditText.getText().toString());
                    if ((channel > 0)&&(channel < 17)) {
                        if (mControllerRadioButton.isChecked()) {
                            int controller =
                                    Integer.parseInt(mControllerEditText.getText().toString());
                            if ((controller >= 0)&&(controller < 128)) {
                                enabled = true;
                            }
                        } else {
                            enabled = true;
                        }
                    }
                } catch (Throwable t) {
                }
            }

            mOkButton.setEnabled(enabled);
        }

        @Override
        protected void recoverResources() {

        }

        @Override
        protected void releaseResources() {

        }

        @Override
        protected void findElements() {
            mNameEditText = (EditText) findViewById(R.id.edittext_name);
            mChannelEditText = (EditText) findViewById(R.id.edittext_channel);
            mTypeRadioGroup = (RadioGroup) findViewById(R.id.radiogroup_type);
            mNoteRadioButton = (RadioButton) findViewById(R.id.radiobutton_note);
            mControllerRadioButton = (RadioButton) findViewById(R.id.radiobutton_controller);
            mControllerEditText = (EditText) findViewById(R.id.edittext_controller);
            mOkButton = (Button) findViewById(R.id.button_ok);
        }

        private void reset(RealPattern pattern, EmptyExecutor emptyExecutor) {
            mPattern = pattern;
            mNameEditText.setText(pattern.mName);
            mChannelEditText.setText(Integer.toString(pattern.mChannel));
            int type = pattern.mType;
            if (type == RealPattern.TYPE_NOTE) {
                mNoteRadioButton.setChecked(true);
                mControllerEditText.setText("");
            } else {
                mControllerRadioButton.setChecked(true);
                mControllerEditText.setText(Integer.toString(pattern.mController));
            }
            mEmptyExecutor = emptyExecutor;
        }

        @Override
        protected void assignHandlers() {
            mTypeRadioGroup.setOnCheckedChangeListener(getOnCheckedChangedListener());
            mOkButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mEmptyExecutor != null) {
                        mEmptyExecutor.execute();
                    }
                    String name = mNameEditText.getText().toString();
                    int channel = 0;
                    int controller = 0;
                    try {
                        channel = Integer.parseInt(mChannelEditText.getText().toString());
                        controller = Integer.parseInt(mControllerEditText.getText().toString());
                    } catch (Throwable t) {
                    }

                    boolean isNote = mNoteRadioButton.isChecked();
                    MainActivity.this.setupPattern(mPattern, name, channel, isNote, controller);
                    dismiss();
                }
            });
            mNameEditText.addTextChangedListener(getTextWatcher());
            mChannelEditText.addTextChangedListener(getTextWatcher());
            mControllerEditText.addTextChangedListener(getTextWatcher());
        }

        private TextWatcher getTextWatcher() {
            if (mTextWatcher == null) {
                mTextWatcher = new TextWatcher() {

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        updateViews();
                    }
                };
            }
            return mTextWatcher;
        }

        private RadioGroup.OnCheckedChangeListener getOnCheckedChangedListener() {
            if (mOnCheckedChangedListener == null) {
                mOnCheckedChangedListener = new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        updateViews();
                    }
                };
            }
            return mOnCheckedChangedListener;
        }


    }
    private class PatternListDialog extends Dialog {
        private PatternListAdapter mPatternListAdapter;
        private List<RealPattern> mPatterns;
        private List<RealPattern> mPatternsExcluded;

        class PatternListAdapter extends BaseAdapter {
            private List<RealPattern> mPatterns;

            @Override
            public int getCount() {
                return mPatterns.size();
            }

            @Override
            public Object getItem(int position) {
                return mPatterns.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                android.widget.TextView result = new android.widget.TextView(MainActivity.this);
                RealPattern pattern = (RealPattern) getItem(position);
                result.setText(pattern.mName);
                return result;
            }

            private void reset(List<RealPattern> patterns) {
                mPatterns = patterns;
            }
        }
        private Button mAddButton;
        private ListView mPatternListListView;

        private PatternListDialog() {
            super(MainActivity.this, R.layout.dialog_patterns, true);
        }

        @Override
        protected void prepareContextMenu(ContextMenu menu, int position) {

        }

        @Override
        protected void setContextItemExecutors() {
            setContextItemExecutor(R.id.menuitem_enterpattern, new ContextItemExecutor() {
                public void execute(MenuItem item) {
                    AdapterView.AdapterContextMenuInfo menuInfo =
                            (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                    RealPattern pattern = mPatternsExcluded.get((int) menuInfo.id);
                    changePattern(pattern);
                    dismiss();
                }
            });
            setContextItemExecutor(R.id.menuitem_deletepattern, new ContextItemExecutor() {
                public void execute(MenuItem item) {
                    AdapterView.AdapterContextMenuInfo menuInfo =
                            (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                    Pattern pattern = mPatternsExcluded.get((int) menuInfo.id);
                    mPatterns.remove(pattern);
                    mPatternsExcluded.remove(pattern);
                    PatternListAdapter patternListAdapter =
                            getPatternListAdapter();
                    patternListAdapter.notifyDataSetChanged();
                    MainActivity.this.updateViews();
                }
            });
        }

        @Override
        protected void registerForContextMenus() {
            registerForContextMenu(mPatternListListView, new ContextMenuCreator() {
                public int createContextMenu(ContextMenu.ContextMenuInfo menuInfo) {
                    return R.menu.context_patterns;
                }
            });
        }

        @Override
        protected void updateViews() {
            PatternListAdapter patternListAdapter =
                    getPatternListAdapter();
            mPatternListListView.setAdapter(patternListAdapter);
        }

        private PatternListAdapter getPatternListAdapter() {
            if (mPatternListAdapter == null) {
                mPatternListAdapter = new PatternListAdapter();
            }
            mPatternListAdapter.reset(mPatternsExcluded);
            return mPatternListAdapter;
        }

        @Override
        protected void recoverResources() {

        }

        @Override
        protected void releaseResources() {

        }

        @Override
        protected void findElements() {
            mPatternListListView = (ListView) findViewById(R.id.listview_patterns);
            mAddButton = (Button) findViewById(R.id.button_add);
        }

        @Override
        protected void assignHandlers() {
            mAddButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleAddPattern();
                }
            });
        }

        private void handleAddPattern() {
            RealPattern pattern = mNewPatternHandler.getNewPattern();
            mPatternsExcluded.add(pattern);
            SetupPatternDialog setupPatternDialog = getSetupPatternDialog();
            setupPatternDialog.reset(pattern, getPatternAddedExecutor());
            showDialog(setupPatternDialog);
        }

        private void reset(List<RealPattern> patterns, Pattern exclude, NewPatternHandler newPatternHandler) {
            mPatterns = patterns;
            mPatternsExcluded = new LinkedList<RealPattern>(patterns);
            if (exclude != null) {
                mPatternsExcluded.remove(exclude);
            }
            mNewPatternHandler = newPatternHandler;
        }
    }
    private class NumberPickerDialog extends Dialog {

        private NumberPickerDialogExecutor mNumberPickerDialogExecutor;

        private NumberPicker mNumberPicker;
        private Button mOkButton;

        private NumberPickerDialog() {
            super(MainActivity.this, R.layout.dialog_numberpicker, true);
        }

        private void reset(int current, NextNumberGetter nextNumberGetter,
                           NumberPickerDialogExecutor numberPickerDialogExecutor) {
            mNumberPicker.setValue(current);
            mNumberPicker.setNextNumberGetter(nextNumberGetter);
            mNumberPickerDialogExecutor = numberPickerDialogExecutor;
        }

        @Override
        protected void prepareContextMenu(ContextMenu menu, int position) {

        }

        @Override
        protected void setContextItemExecutors() {

        }

        @Override
        protected void registerForContextMenus() {

        }

        @Override
        protected void updateViews() {

        }

        @Override
        protected void recoverResources() {

        }

        @Override
        protected void releaseResources() {

        }

        @Override
        protected void findElements() {
            mNumberPicker = (NumberPicker) findViewById(R.id.numberpicker);
            mOkButton = (Button) findViewById(R.id.button_ok);
        }

        @Override
        protected void assignHandlers() {
            mOkButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mNumberPickerDialogExecutor.execute(mNumberPicker.getValue());
                    dismiss();
                }
            });
        }
    }
    private class ServerDialog extends Dialog {
        private EditText mServerEditText;
        private Button mTestButton;
        private Button mOkButton;
        private String mServer;
        private int mPort;
        private String mServerString;

        public ServerDialog() {
            super(MainActivity.this, R.layout.dialog_server, true);
        }

        @Override
        protected void prepareContextMenu(ContextMenu menu, int position) {

        }

        @Override
        protected void setContextItemExecutors() {

        }

        @Override
        protected void registerForContextMenus() {

        }

        @Override
        protected void updateViews() {

        }

        @Override
        protected void recoverResources() {

        }

        @Override
        protected void releaseResources() {

        }

        @Override
        protected void findElements() {
            mServerEditText = (EditText) findViewById(R.id.edittext_server);
            mTestButton = (Button) findViewById(R.id.button_test);
            mOkButton = (Button) findViewById(R.id.button_ok);
        }

        @Override
        protected void assignHandlers() {
            mServerEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    checkButtonsEnabled();
                }
            });
            mTestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity.this.testServer(mServer, mPort);
                }
            });
            mOkButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity.this.setServer(mServerString);
                    dismiss();
                }
            });

        }

        private void checkButtonsEnabled() {
            mServerString = mServerEditText.getText().toString();
            boolean enabled = false;
            try {
                mServer = getServer(mServerString);
                mPort = getPort(mServerString);
                enabled = true;
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            mOkButton.setEnabled(enabled);
            mTestButton.setEnabled(enabled);
        }

        public void reset(String serverString) {
            mServerEditText.setText(serverString);
        }
    }
    private class RandomiseDialog extends Dialog {
        private final ViewGroup.LayoutParams mLayoutParams = new
                LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1);
        private RealPattern mPattern;
        private LinearLayout mOffByLayout;
        private List<OffByCheckBox> mOffByCheckBoxes =
                new LinkedList<>();
        private EditText mProbabilityEditText;
        private EditText mSlideProbabilityEditText;
        private Button mOkButton;
        private TextWatcher mTextWatcher;
        private int mProbabilityPercentage;
        private int mSlideProbabilityPercentage;

        private void checkOkEnabled() {
            mProbabilityPercentage =
                    getPercentage(mProbabilityEditText.getText().toString());
            mSlideProbabilityPercentage =
                    getPercentage(mSlideProbabilityEditText.getText().toString());
            boolean enabled = ((mProbabilityPercentage >= 0) && (mProbabilityPercentage <= 100));
            enabled = enabled && ((mSlideProbabilityPercentage >= 0) && (mSlideProbabilityPercentage <= 100));
            mOkButton.setEnabled(enabled);
        }

        private int getPercentage(String string) {
            int result = -1;
            try {
                result = Integer.parseInt(string);
            } catch (NumberFormatException nfe) {
            }

            return result;
        }

        public RandomiseDialog() {
            super(MainActivity.this, R.layout.dialog_randomise, true);

        }

        public void reset(RealPattern pattern) {
            this.mPattern = pattern;
            mOffByLayout.removeAllViews();
            mOffByCheckBoxes.clear();
            Bar bar = mPattern.mBars.get(0);
            boolean first = true;
            for (Step step : bar.mSteps) {
                RealStep realStep = (RealStep) step;
                if ((!first)&&(realStep.mOffsetFromParent < 1)) {
                    break;
                }
                first = false;
                OffByCheckBox offByCheckBox = new OffByCheckBox();

            }
            mProbabilityEditText.setText("50");
            mSlideProbabilityEditText.setText("50");
            mSlideProbabilityEditText.setEnabled(mPattern.isNote());
        }

        @Override
        protected void prepareContextMenu(ContextMenu menu, int position) {

        }

        @Override
        protected void setContextItemExecutors() {

        }

        @Override
        protected void registerForContextMenus() {

        }

        @Override
        protected void updateViews() {

        }

        @Override
        protected void recoverResources() {

        }

        @Override
        protected void releaseResources() {

        }

        @Override
        protected void findElements() {
            mOffByLayout = (LinearLayout) findViewById(R.id.layout_offby);
            mProbabilityEditText = (EditText) findViewById(R.id.edittext_probability);
            mSlideProbabilityEditText = (EditText) findViewById(R.id.edittext_slideprobability);
            mOkButton = (Button) findViewById(R.id.button_ok);
        }

        @Override
        protected void assignHandlers() {
            mProbabilityEditText.addTextChangedListener(getTextWatcher());
            mSlideProbabilityEditText.addTextChangedListener(getTextWatcher());
            mOkButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity.this.randomise(mPattern, mProbabilityPercentage,
                            mSlideProbabilityPercentage, mOffByCheckBoxes);
                    dismiss();
                }
            });

        }

        private TextWatcher getTextWatcher() {
            if (mTextWatcher == null) {
                mTextWatcher = new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        checkOkEnabled();
                    }
                };
            }
            return mTextWatcher;
        }

        private class OffByCheckBox extends CheckBox {
            public OffByCheckBox() {
                super(MainActivity.this);
                setLayoutParams(mLayoutParams);
                RandomiseDialog.this.mOffByLayout.addView(this);
                RandomiseDialog.this.mOffByCheckBoxes.add(this);
                setChecked(true);

            }
        }
    }
}
