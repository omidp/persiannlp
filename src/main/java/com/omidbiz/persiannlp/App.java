package com.omidbiz.persiannlp;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import opennlp.tools.doccat.DoccatFactory;
import opennlp.tools.doccat.DoccatModel;
import opennlp.tools.doccat.DocumentCategorizerME;
import opennlp.tools.doccat.DocumentSample;
import opennlp.tools.doccat.DocumentSampleStream;
import opennlp.tools.langdetect.Language;
import opennlp.tools.langdetect.LanguageDetector;
import opennlp.tools.langdetect.LanguageDetectorME;
import opennlp.tools.langdetect.LanguageDetectorModel;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.NameSample;
import opennlp.tools.namefind.NameSampleDataStream;
import opennlp.tools.namefind.TokenNameFinderFactory;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.InputStreamFactory;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;
import opennlp.tools.util.Span;
import opennlp.tools.util.TrainingParameters;

/**
 * @author omidp
 *
 */
public class App
{

    public static final String USER_HOME = System.getProperty("user.home");

    public static void main(String[] args) throws IOException
    {
        // detectLang();
        // detectSentence();
        // tokenization();
        // System.out.println("english name finder");
        // nameFinder();
        // trainNameFinder();
        // System.out.println("persian name finder");
        // persianNameFinder();

        // categorization

//        doccattrain();
//        doccat();

    }

    private static void doccat() throws IOException
    {
        DoccatModel m = new DoccatModel(App.class.getResourceAsStream("/fa-doccat.bin"));
        DocumentCategorizerME myCategorizer = new DocumentCategorizerME(m);
        double[] outcomes = myCategorizer.categorize(new String[]{"کنسول" , "PS4", "عالیه", "حرف نداره"});
        String category = myCategorizer.getBestCategory(outcomes);
        System.out.println(category);
    }

    private static void doccattrain() throws IOException, FileNotFoundException
    {
        DoccatModel model = null;
        InputStreamFactory dataIn = new FileStreamFactory("/fa-doccat.train");
        ObjectStream<String> lineStream = new PlainTextByLineStream(dataIn, StandardCharsets.UTF_8);
        ObjectStream<DocumentSample> sampleStream = new DocumentSampleStream(lineStream);

        model = DocumentCategorizerME.train("fa", sampleStream, TrainingParameters.defaultParams(), new DoccatFactory());
        try (OutputStream modelOut = new BufferedOutputStream(new FileOutputStream(new File(USER_HOME+"/fa-doccat.bin"))))
        {
            model.serialize(modelOut);
        }
    }

    private static void persianNameFinder() throws IOException, FileNotFoundException
    {
        try (InputStream modelIn = App.class.getResourceAsStream("/fa-ner-person.bin"))
        {
            TokenNameFinderModel model = new TokenNameFinderModel(modelIn);
            NameFinderME nameFinder = new NameFinderME(model);
            String sentence[] = new String[] { "Pierre", "Vinken", "is", "61", "years", "old", ".", "امید" };

            Span nameSpans[] = nameFinder.find(sentence);
            Arrays.asList(nameSpans).forEach(i -> {
                System.out.println(sentence[i.getStart()]);
                System.out.println(sentence[i.getEnd() - 1]);
                System.out.println(i.getType());
            });

        }
    }

    private static void trainNameFinder() throws IOException, FileNotFoundException
    {
        ObjectStream<String> lineStream = new PlainTextByLineStream(new FileStreamFactory("/fa-ner-person.train"), StandardCharsets.UTF_8);

        TokenNameFinderModel model;

        try (ObjectStream<NameSample> sampleStream = new NameSampleDataStream(lineStream))
        {
            model = NameFinderME.train("fa", "person", sampleStream, TrainingParameters.defaultParams(), new TokenNameFinderFactory());
        }

        try (BufferedOutputStream modelOut = new BufferedOutputStream(new FileOutputStream(new File(USER_HOME + "/fa-ner-person.bin"))))
        {
            model.serialize(modelOut);
        }
    }

    private static void nameFinder() throws IOException, FileNotFoundException
    {
        File f = new File(USER_HOME + "/en-ner-person.bin");
        if (f.exists() == false)
            throw new IllegalArgumentException("en-ner-person.bin not found,  download and put it here. " + f.getAbsolutePath());
        try (InputStream modelIn = new FileInputStream(USER_HOME + "/en-ner-person.bin"))
        {
            TokenNameFinderModel model = new TokenNameFinderModel(modelIn);
            NameFinderME nameFinder = new NameFinderME(model);
            String sentence[] = new String[] { "Pierre", "Vinken", "is", "61", "years", "old", ".", "امید" };

            Span nameSpans[] = nameFinder.find(sentence);
            Arrays.asList(nameSpans).forEach(i -> {
                System.out.println(sentence[i.getStart()]);
                System.out.println(sentence[i.getEnd() - 1]);
                System.out.println(i.getType());
            });

        }
    }

    private static void tokenization() throws IOException, FileNotFoundException
    {
        try (InputStream modelIn = App.class.getResourceAsStream("/en-token.bin"))
        {
            TokenizerModel model = new TokenizerModel(modelIn);
            Tokenizer tokenizer = new TokenizerME(model);
            String tokens[] = tokenizer.tokenize("An input sample sentence.");
            Arrays.asList(tokens).forEach(item -> {
                System.out.println(item);
            });

            String pestokens[] = tokenizer.tokenize(".یه نمونه جمله");
            Arrays.asList(pestokens).forEach(item -> {
                System.out.println(item);
            });
        }
    }

    private static void detectSentence() throws IOException, FileNotFoundException
    {
        try (InputStream modelIn = App.class.getResourceAsStream("/en-sent.bin"))
        {
            SentenceModel model = new SentenceModel(modelIn);
            SentenceDetectorME sentenceDetector = new SentenceDetectorME(model);
            String sentences[] = sentenceDetector.sentDetect("  First sentence. Second sentence. ");
            for (String string : sentences)
            {
                System.out.println(string);
            }
            String pessentences[] = sentenceDetector.sentDetect("  جمله ی اول. جمله ی دوم. ");
            for (String string : pessentences)
            {
                System.out.println(string);
            }

        }
    }

    private static void detectLang() throws IOException
    {
        System.out.println("Hello World!");
        LanguageDetectorModel m = new LanguageDetectorModel(new File("/home/omidp/nlp/langdetect-183.bin"));
        //
        LanguageDetector myCategorizer = new LanguageDetectorME(m);
        // Get the most probable language
        Language bestLanguage = myCategorizer.predictLanguage("hi this is test for a simple language detection");
        System.out.println("Best language: " + bestLanguage.getLang());
        System.out.println("Best language confidence: " + bestLanguage.getConfidence());
        //
        bestLanguage = myCategorizer.predictLanguage("سلام این یک تست است برای پیداکردن زبان متن");
        System.out.println("Best language: " + bestLanguage.getLang());
        System.out.println("Best language confidence: " + bestLanguage.getConfidence());
    }

    public static class FileStreamFactory implements InputStreamFactory
    {

        private String fileName;

        public FileStreamFactory(String fileName)
        {
            this.fileName = fileName;
        }

        @Override
        public InputStream createInputStream() throws IOException
        {
            return App.class.getResourceAsStream(fileName);
        }

    }

}
