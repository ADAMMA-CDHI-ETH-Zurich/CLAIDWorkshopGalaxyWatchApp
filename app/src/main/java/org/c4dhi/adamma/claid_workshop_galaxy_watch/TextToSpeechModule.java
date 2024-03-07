package org.c4dhi.adamma.claid_workshop_galaxy_watch;

import android.speech.tts.TextToSpeech;

import java.util.Locale;
import java.util.Map;

import adamma.c4dhi.claid.Module.ChannelData;
import adamma.c4dhi.claid.Module.Module;
import adamma.c4dhi.claid.Module.ModuleAnnotator;
import adamma.c4dhi.claid.Module.PropertyHelper.PropertyHelper;
import adamma.c4dhi.claid_platform_impl.CLAID;

public class TextToSpeechModule extends Module implements TextToSpeech.OnInitListener
{
    private String speechLanguage = "";
    private TextToSpeech textToSpeech;
    public static void annotateModule(ModuleAnnotator annotator)
    {
        annotator.setModuleCategory("DataCollection");
        annotator.setModuleDescription("The GyroscopeCollector allows to record gyrpscpüe data using the devices built-in gyroscope of the device."
                + "The sampling frequency can be freely configured, however is subject to the limitations of the device (i.e., built-in sensor speicifications)."
                + "The GyroscopeCollector features two recording modes: \"Batched\" and \"Streaming\"\n");


        annotator.describeProperty("SpeechLanguage", "Language used by the Speech engine. Defines intonation and pronounciation of individual words.",
                annotator.makeEnumProperty(new String[]{"US",  "CANADA", "CANADA_FRENCH","GERMANY", "ITALY", "JAPAN", "CHINA"}));

        annotator.describeSubscribeChannel("TextToSpeak", String.class, "Channel with incoming text to be spoken by the Text to Speak engine.");
    }


    public void initialize(Map<String, String> propertiesMap)
    {
        PropertyHelper propertyHelper = new PropertyHelper(propertiesMap);
        this.speechLanguage = propertyHelper.getProperty("samplingFrequency", String.class);

        if(propertyHelper.wasAnyPropertyUnknown())
        {
            this.moduleFatal(propertyHelper.getMissingPropertiesErrorString());
            return;
        }

        textToSpeech = new TextToSpeech(CLAID.getContext(), this);
    }

    Locale getLocaleFromProperty()
    {
        if(this.speechLanguage.toUpperCase().equals("US"))
        {
            return Locale.US;
        }
        else if (this.speechLanguage.toUpperCase().equals("CANADA"))
        {
            return Locale.CANADA;
        }
        else if (this.speechLanguage.toUpperCase().equals("CANADA_FRENCH"))
        {
            return Locale.CANADA_FRENCH;
        }
        else if (this.speechLanguage.toUpperCase().equals("GERMANY"))
        {
            return Locale.GERMANY;
        }
        else if (this.speechLanguage.toUpperCase().equals("ITALY"))
        {
            return Locale.ITALY;
        }
        else if (this.speechLanguage.toUpperCase().equals("JAPAN"))
        {
            return Locale.JAPAN;
        }
        else if (this.speechLanguage.toUpperCase().equals("CHINA"))
        {
            return Locale.CHINA;
        }
        else
        {
            moduleError("Invalid property \"speechLanguage\". Language \"" + this.speechLanguage + "\"" +
                    " is not supported. Expected one of [\"US\",  \"CANADA\", \"CANADA_FRENCH\",\"GERMANY\", \"ITALY\", \"JAPAN\", \"CHINA\"].");
            return null;
        }
    }

    public void onData(ChannelData<String> data)
    {
        String text = data.getData();
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            // Set language (Optional)
            int result = textToSpeech.setLanguage(getLocaleFromProperty());

            if (result == TextToSpeech.LANG_MISSING_DATA ||
                    result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Handle language not supported or missing data
            } else {
                // Text-to-Speech is ready
                // Call the method to convert text to speech
                String textToRead = "Hello, this is a test.";
                textToSpeech.speak(textToRead, TextToSpeech.QUEUE_FLUSH, null, null);
            }
        } else {
            // Initialization failed
            // Handle initialization failure
        }
    }

    @Override
    protected void terminate() {
        // Shutdown the Text-to-Speech engine when the activity is destroyed
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }
}
