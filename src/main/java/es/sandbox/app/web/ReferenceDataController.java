package es.sandbox.app.web;

import es.sandbox.app.web.control.Option;
import es.sandbox.app.web.control.Selector;
import es.sandbox.app.web.control.Transformer;
import es.sandbox.app.web.control.Transformer.NopTransformer;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;

import static es.sandbox.app.web.control.Selector.SelectorBuilder;
import static es.sandbox.app.web.control.Selector.builderForPath;
import static java.lang.String.format;

/**
 * Created by jeslopalo on 24/04/15.
 */
@RestController
@RequestMapping("/data")
public class ReferenceDataController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReferenceDataController.class);

    private final Transformer transformer;

    public ReferenceDataController() {
        this.transformer = new NopTransformer();
    }

    @Inject
    public ReferenceDataController(final Transformer transformer) {
        this.transformer = transformer;
    }

    @RequestMapping(value = "/first-values", method = RequestMethod.GET)
    public Selector getFirstValues(@RequestParam(value = "path", required = true) final String path, @RequestParam(value = "populate-path", required = true) final String populatePath) {
        LOGGER.debug("Loading options for [{}] path and on select populate [{}] path...", path, populatePath);

        return builderForPath(path)
                .onSelect(populatePath, "http://localhost:8080/data/second-values?path=" + populatePath + "&populate-path=thirdValue&firstValue=%s")
                .withTransformer(this.transformer)
                .withOptions(
                        new Option("Choose one..."),
                        new Option("Option: value 1", 1))
/*
                        new Option("Option: value 2", 2),
                        new Option("Option: value 3", 3))
*/
                .build();
    }

    @RequestMapping(value = "/second-values", method = RequestMethod.GET)
    public Selector getSecondValues(@RequestParam(value = "path", required = true) final String path, @RequestParam(value = "populate-path", required = true) final String populatePath, @RequestParam("firstValue") final String firstValue) {
        LOGGER.debug("Loading options for [{}] path and on select populate [{}] path (selected firstValue=[{}])...", path, populatePath, firstValue);

        final SelectorBuilder selectorBuilder = builderForPath(path)
                .onSelect(populatePath, "http://localhost:8080/data/third-values?path=" + populatePath + "&firstValue=" + firstValue + "&secondValue=%s")
                .withTransformer(this.transformer)
                .addOption(new Option("Choose one..."));

        if (NumberUtils.isNumber(firstValue)) {
            selectorBuilder
                    .addOption(new Option(format("Option: %s value 1", firstValue), calculate(1, firstValue)));
/*
                    .addOption(new Option(format("Option: %s value 2", firstValue), calculate(2, firstValue)))
                    .addOption(new Option(format("Option: %s value 3", firstValue), calculate(3, firstValue)));
*/
        }

        return selectorBuilder.build();
    }

    @RequestMapping(value = "/third-values", method = RequestMethod.GET)
    public Selector getThirdValues(@RequestParam(value = "path", required = true) final String path, @RequestParam("firstValue") final String firstValue, @RequestParam("secondValue") final String secondValue) {
        LOGGER.debug("Loading options for [{}] path (selected firstValue=[{}], secondValue=[{}])...", path, firstValue, secondValue);

        final SelectorBuilder selectorBuilder = builderForPath(path)
                .withTransformer(this.transformer)
                .addOption(new Option("Choose one..."));

        if (NumberUtils.isNumber(firstValue) && NumberUtils.isNumber(secondValue)) {
            selectorBuilder
                    .addOption(new Option(format("Option: %s %s value 1", firstValue, secondValue), calculate(1, firstValue, secondValue)));
/*
                    .addOption(new Option(format("Option: %s %s value 2", firstValue, secondValue), calculate(2, firstValue, secondValue)))
                    .addOption(new Option(format("Option: %s %s value 3", firstValue, secondValue), calculate(3, firstValue, secondValue)));
*/
        }

        return selectorBuilder.build();
    }

    private int calculate(final int value, final String... values) {
        int sum = value;
        for (String stringValue : values) {
            sum += NumberUtils.toInt(stringValue);
        }
        return sum;
    }
}
