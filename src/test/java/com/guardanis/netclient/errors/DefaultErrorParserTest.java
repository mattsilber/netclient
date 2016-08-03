package com.guardanis.netclient.errors;

import android.content.Context;

import com.guardanis.netclient.WebResult;

import junit.framework.TestCase;

import org.junit.Test;
import static org.mockito.Mockito.*;

import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class DefaultErrorParserTest {

    @Mock
    Context mockedContext;

    @Test
    public void testBaseErrorParsingMessages() throws Exception {
        WebResult result = new WebResult(400, "{\"errors\":{\"base\":[\"Some base error occurred\"]}}\n");

        List<String> errorMessages = new DefaultErrorParser()
                .parseErrorMessages(mockedContext, result);

        ApiError errors = new ApiError(result, errorMessages);

        assert(errors.hasErrors());
        assert(errors.getErrors().get(0).equals("Some base error occurred."));
    }

    @Test
    public void testSpecificErrorParsingMessages() throws Exception {
        WebResult result = new WebResult(400, "{\"errors\":{\"key\":[\"has some issue\"]}}\n");

        List<String> errorMessages = new DefaultErrorParser()
                .parseErrorMessages(mockedContext, result);

        ApiError errors = new ApiError(result, errorMessages);

        assert(errors.hasErrors());
        assert(errors.getErrors().get(0).equals("Key has some issue."));
    }

    @Test
    public void testMultiSpecificErrorParsingMessages() throws Exception {
        WebResult result = new WebResult(400, "{\"errors\":{\"key\":[\"has some issue\", \"has some other issue\"]}}\n");

        List<String> errorMessages = new DefaultErrorParser()
                .parseErrorMessages(mockedContext, result);

        ApiError errors = new ApiError(result, errorMessages);

        assert(errors.hasErrors());
        assert(errors.toString().equals("Key has some issue.\nKey has some other issue."));
    }

}