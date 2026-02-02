package rs.igapp.aurora.server.service;

import rs.igapp.aurora.api.dto.request.LogEventRequest;
import rs.igapp.aurora.api.dto.response.LogEventResponse;
import rs.igapp.aurora.domain.entity.LogEvent;
import rs.igapp.aurora.domain.entity.Severity;
import rs.igapp.aurora.domain.entity.Source;
import rs.igapp.aurora.persistence.repository.LogEventRepository;
import rs.igapp.aurora.persistence.repository.SeverityRepository;
import rs.igapp.aurora.persistence.repository.SourceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * LogEventService - Poslovna logika za menadzment sigurnostnih log dogadjaja u SIEM sistemu
 * 
 * Svrha:
 * Ovaj servis drzi sve operacije povezane za log eventove (pr. pokusaj ulogovanja, sistemske greske, sumnjive radnje)
 * Stoji izmedju REST API-ja (kontrolerra) i baze (repozitorijuma)
 * 	Samim tim menadzuje sa poslovnim pravilima i transformacijom podataka

 * STA TACNO RADI?:
 * 1. Kreira nove logove dogadjaja kada AGENT posalje podatke
 * 2. Povlaci logove kroz razne filtere (izvor, bitnost (severity), vremensi opseg)
 * 3. Pretrazuje dogadjaje po kljucnim recima
 * 4. Pretvara unose iz baze u API odgovarajuce formate (DTOS)
 * 5. Azurira postojece logove ako je potrebno.
 * 
 * NASLEDJIVANJE:
 * Produzuje CrudService apstraktnu klasu koja ima CRUD operacije 
 * Ovaj servis jos dodaje specijalizovanu pretragu i filterovanje pored tih osnovnih crud operacija
 * 
 * PRIMER TOKA:
 * AGENT salje log -> Kotroler prima -> LogEventService procesuira -> Repozitorijum sacuva na bazu
 */
@Service 
public class LogEventService extends CrudService<LogEvent, LogEventRequest, LogEventResponse, Long> {

    private final LogEventRepository logEventRepository;  
    private final SeverityRepository severityRepository; 
    private final SourceRepository sourceRepository;  

    public LogEventService(LogEventRepository logEventRepository, 
                          SeverityRepository severityRepository,
                          SourceRepository sourceRepository) {
        super(logEventRepository); 
        this.logEventRepository = logEventRepository;
        this.severityRepository = severityRepository;
        this.sourceRepository = sourceRepository;
    }

    // ==================== METODE PRETRAGE ====================
    // ove metode dodaju specijalizovan nacin da pronadju logove dogadjaja (prosirenije nego obican CRUD)
    
    @Transactional(readOnly = true) //pretraga po izvoru
    public Page<LogEventResponse> getBySource(String sourceId, Pageable pageable) {
        return logEventRepository.findBySource_AgentId(sourceId, pageable)
            .map(this::mapToResponse);  
    }

    @Transactional(readOnly = true) // pretraga po bitnosti (severity)
    public Page<LogEventResponse> getBySeverity(Long severityId, Pageable pageable) {
        return logEventRepository.findBySeverity_Id(severityId, pageable)
            .map(this::mapToResponse);
    }


    @Transactional(readOnly = true) // preetraga po vremenskom opsegu
    public List<LogEventResponse> getByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return logEventRepository.findByTimestampRange(startTime, endTime).stream()
            .map(this::mapToResponse)  // Convert each entity to response format
            .toList();  // Collect stream back into a list
    }

    @Transactional(readOnly = true) // pretraga po kljucnoj reci
    public Page<LogEventResponse> search(String keyword, Pageable pageable) {
        return logEventRepository.searchByKeyword(keyword, pageable)
            .map(this::mapToResponse);
    }


    @Transactional(readOnly = true) //broj sigurnosnih rizika po bitnosti
    public long countBySeverity(Long severityId) {
        return logEventRepository.countBySeverity_Id(severityId);
    }

    // ==================== IMPLEMENTACIJA APSTRAKTNIH METODA ====================

    @Override
    protected LogEvent mapToEntity(LogEventRequest request) {
    	// korak 1: Pronadji izvor po agentId
    	// Ako izvor ne postoji, baca gresku (ne mozemo praviti log ako ne postoji izvor)
        Source source = sourceRepository.findById(request.getSourceId())
            .orElseThrow(() -> new RuntimeException("Source not found: " + request.getSourceId()));
        //KORAK 2: Pronaci bitnost ako je prilozena (opcionalno)
        // Ako je severityID null, onda bitnost(ozbiljnost) ostaje null (npr. informacioni log)
        Severity severity = request.getSeverityId() != null 
            ? severityRepository.findById(request.getSeverityId()).orElse(null)
            : null;
        // Korak 3: Sagraditi LogEvent entity 
        return LogEvent.builder()
            .source(source)           // Povezati na Source entity
            .message(request.getMessage())  // Kopirati tekst poruke
            .severity(severity)       // Povezati na Severity entity (ILI null)
            .rawData(request.getRawData())  // Kopirati raw JSON data
            .timestamp(request.getTimestamp() != null ? request.getTimestamp() : LocalDateTime.now())  // Koristiti prilozeno vreme ILI trenutno vreme ako nije prilozeno nista
            .build();
    }

    /**
     * Konvertovati format entiteta iz base u API repsonse format (Za operacije citanja)
     * 
     * STA SE DESI:
     * 1. Repozitorijum izvlaci LogEvent iz baze podataka (Sa vezama)
     * 2. Ovaj "metod" pretvara ga pretvara u LogEventResponse (obican JSON format)
     * 3. API vraca JSON klijentu (frontendu)

     * 
     * Zasto ga pretvaram? :
     * API odgovori trebaju da budu jednostavni (NEMA "KRUZNIH REFERENCI")
     * Frontendu ne treba cela veza izmedju entiteta
     * Pretvoriti objekte u obicne stringove (npr. Severity object -> "CRITICAL" string)
     */
    @Override
    protected LogEventResponse mapToResponse(LogEvent logEvent) {
        return LogEventResponse.builder()
            .id(logEvent.getId())  // Kopira ID
            // Pretvara Source entitet u obican agentID string 
            .sourceId(logEvent.getSource() != null ? logEvent.getSource().getId() : null)
            .message(logEvent.getMessage())  // Kopira poruku
            // Pretvara Severity entitet u obican name string 
            .severity(logEvent.getSeverity() != null ? logEvent.getSeverity().getName() : null)
            .rawData(logEvent.getRawData())  // kopira raw JSON	
            .timestamp(logEvent.getTimestamp())  // Kopira vreme
            .createdAt(logEvent.getCreatedAt())  // Kopira vreme kreacije
            .build();
    }

    /**
     * Azurira postojecu bazu podataka entiteta sa novim podacija iz API zahteva (Za UPDATE opewaciju)
     * 
     * STA SE DESI?:
     * 1. Repozitorijum pronadje postojeci LogEvent po ID-u
     * 2. Ova metoda azururia samo polja koja su prilozena u zahtevu
     * 3. Repozittorijum sacuva azurirani entitet nazad u bazu podataka.
     * 
     * BUSINESS RULES:
     * - Samo azuriraj polja koja nisu null
     * - Ako se sourceId promeni, pronaci now Source entitet
     * - Ako se severityId promeni, pronaci novi Severity entitet
     * - Odrzati originalne vrednosti ako polja koja ne menjamo su null
     */
    @Override
    protected void updateEntity(LogEvent entity, LogEventRequest request) {
    	// Korak 1: Azurirati izvor ako je prilozen
        if (request.getSourceId() != null) {
            // Pokusati pronaci novi izvor, ako ga nema zadrzavamo stari
            Source source = sourceRepository.findById(request.getSourceId())
                .orElse(entity.getSource());
            entity.setSource(source);
        }
        // Korak 2: Azuriranje poruke (UVEK azurirati, obavezno polje)

        entity.setMessage(request.getMessage());
        // Korak 3: Azurirati raw podatke (uvek azurirati, moze biti null)
        entity.setRawData(request.getRawData());
        // KORAK 4: Azurirati ozbiljnost ako je prilozena
        if (request.getSeverityId() != null) {
            entity.setSeverity(severityRepository.findById(request.getSeverityId()).orElse(null));
        }
        // Korak 5: Azurirati vreme ako je prilozeno
        if (request.getTimestamp() != null) {
            entity.setTimestamp(request.getTimestamp());
        }
        

    }
}
