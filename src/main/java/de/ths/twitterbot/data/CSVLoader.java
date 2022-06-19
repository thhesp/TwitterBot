package de.ths.twitterbot.data;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CSVLoader {

    private static final Logger LOG = LoggerFactory.getLogger(CSVLoader.class);

    @Autowired
    private CSVConfigProperties csvConfigProperties;


    public List<CSVEntryDTO> loadCSVData(){
        if(StringUtils.isEmpty(csvConfigProperties.getFilepath())){
            throw new RuntimeException("No configuration for the filepath provided.");
        }

        LOG.info("Loading CSV file: {}", csvConfigProperties.getFilepath());

        try {
            Reader reader = Files.newBufferedReader(Paths.get(
                    ResourceUtils.getFile(csvConfigProperties.getFilepath()).toURI()));
            try (CSVReader csvReader = new CSVReader(reader)) {
                return csvReader.readAll().stream()
                        .map(this::convertEntry)
                        .collect(Collectors.toList());
            } catch (CsvException e) {
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private CSVEntryDTO convertEntry(String[] entry){
        return new CSVEntryDTO(entry[0], entry[1], entry[2]);
    }
}
