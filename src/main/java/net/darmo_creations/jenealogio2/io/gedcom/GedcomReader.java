package net.darmo_creations.jenealogio2.io.gedcom;

import net.darmo_creations.jenealogio2.io.*;
import net.darmo_creations.jenealogio2.model.*;
import net.darmo_creations.jenealogio2.utils.*;
import org.jetbrains.annotations.*;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.text.*;
import java.util.*;
import java.util.stream.*;

public class GedcomReader {
  public static final String FORM_TYPE = "LINEAGE-LINKED";
  public static final String VERSION = "5.5.5";

  public static FamilyTree read(@NotNull Path file) throws IOException, ParseException {
    final FamilyTree familyTree = new FamilyTree(FileUtils.splitExtension(file.getFileName().toString()).left());

    final Result result = parseHeader(file);
    final String lineTerminator = result.lineTerminator();
    final Charset charset = switch (result.encoding()) {
      case UTF8 -> StandardCharsets.UTF_8;
      case UTF16 -> StandardCharsets.UTF_16;
    };
    final int skipLines = result.skipLines();

    final LinkedList<GedcomRecord> recordStack = new LinkedList<>();
    int lineI = 0;

    try (BufferedReader reader = Files.newBufferedReader(file, charset)) {
      Pair<String, Optional<String>> split;

      while ((split = readLineWithTerm(reader)) != null) {
        if (lineI <= skipLines)
          continue;

        String line = split.left();
        final Optional<String> term = split.right();

        // Check line terminator
        if (term.isEmpty())
          throw new ParseException("Missing line terminator", lineI);
        if (!lineTerminator.equals(term.get()))
          throw new ParseException("Line terminator mismatch: expected %s, got %s".formatted(escape(lineTerminator), escape(term.get())), lineI);

        final GedcomRecord gr = new GedcomRecord(line);

        // Basic syntax errors handling
        ensureValidRecordLevel(recordStack, gr, lineI);

        if (gr.level() == 0)
          recordStack.clear();
        recordStack.push(gr);

        // TODO parse rest of file

        lineI++;
      }
    }

    // Final syntax checks
    if (recordStack.isEmpty())
      throw new ParseException("Empty file", 0);
    else if (!recordStack.get(0).equals(new GedcomRecord("0 TRLR")))
      throw new ParseException("Missing closing TRLR tag", lineI);

    return familyTree;
  }

  public static Result parseHeader(@NotNull Path file) throws IOException, ParseException {
    String lineTerminator = null;
    boolean versionFound = false, formVersionFound = false, sourceFound = false;
    FileEncoding encoding = null;
    final LinkedList<GedcomRecord> recordStack = new LinkedList<>();
    final Set<String> visitedTags = new HashSet<>();
    int lineI = 0;

    try (BufferedReader reader = Files.newBufferedReader(file)) {
      Pair<String, Optional<String>> split;

      loop:
      while ((split = readLineWithTerm(reader)) != null) {
        String line = split.left();
        final Optional<String> term = split.right();

        // Check line terminator
        if (term.isEmpty())
          throw new ParseException("Missing line terminator", lineI);
        if (lineTerminator != null && !lineTerminator.equals(term.get()))
          throw new ParseException("Line terminator mismatch: expected %s, got %s".formatted(escape(lineTerminator), escape(term.get())), lineI);

        if (lineI == 0) { // Check for BOM
          if (line.startsWith("\ufeff"))
            line = line.substring(1);
          else
            throw new ParseException("Missing BOM", lineI);
          lineTerminator = term.get();
        }

        final GedcomRecord gr = new GedcomRecord(line);

        // Basic syntax errors handling
        if (lineI == 0 && !gr.tagName().equals("HEAD")) // First tag is always HEAD
          throw new ParseException("Expected tag HEAD, got %s".formatted(gr.tagName()), lineI);
        ensureValidRecordLevel(recordStack, gr, lineI);

        if (gr.level() == 0)
          recordStack.clear();
        recordStack.push(gr);

        final String tagPath = getTagPath(recordStack);

        if (visitedTags.contains(tagPath))
          throw new ParseException("Duplicate tag found: %s".formatted(tagPath), lineI);
        visitedTags.add(tagPath);

        switch (tagPath) {
          case "HEAD", "HEAD.GEDC" -> ensureNoXrefIdNorLineValue(gr, tagPath, lineI);
          case "HEAD.GEDC.VERS" -> {
            ensureNoXrefIdButLineValue(gr, tagPath, lineI);
            final String version = gr.lineValue().get();
            if (!version.equals(VERSION))
              throw new ParseException("Unsupported GEDCOM version %s".formatted(version), lineI);
            versionFound = true;
          }
          case "HEAD.GEDC.FORM" -> {
            if (gr.xrefId().isPresent() || !gr.lineValue().map(s -> s.equals(FORM_TYPE)).orElse(false))
              throw new ParseException("Invalid %s tag".formatted(tagPath), lineI);
          }
          case "HEAD.GEDC.FORM.VERS" -> {
            ensureNoXrefIdButLineValue(gr, tagPath, lineI);
            final String formVersion = gr.lineValue().get();
            if (!formVersion.equals(VERSION))
              throw new ParseException("Unsupported form version %s".formatted(formVersion), lineI);
            formVersionFound = true;
          }
          case "HEAD.CHAR" -> {
            ensureNoXrefIdButLineValue(gr, tagPath, lineI);
            final String enc = gr.lineValue().get();
            encoding = switch (enc) {
              case "UTF-8" -> FileEncoding.UTF8;
              case "UNICODE" -> FileEncoding.UTF16;
              default -> throw new ParseException("Unsupported file encoding %s".formatted(enc), lineI);
            };
          }
          case "HEAD.DEST" -> {
            ensureNoXrefIdButLineValue(gr, tagPath, lineI);
            // TODO
          }
          case "HEAD.SOUR" -> {
            ensureNoXrefIdButLineValue(gr, tagPath, lineI);
            sourceFound = true;
          }
          case "HEAD.SOUR.VERS" -> {
            ensureNoXrefIdButLineValue(gr, tagPath, lineI);
            // TODO
          }
          case "HEAD.SOUR.NAME" -> {
            ensureNoXrefIdButLineValue(gr, tagPath, lineI);
            // TODO
          }
          case "HEAD.SOUR.CORP" -> {
            ensureNoXrefIdButLineValue(gr, tagPath, lineI);
            // TODO
          }
          // TODO HEAD.SOUR.CORP sub-records
          case "HEAD.SOUR.DATA" -> {
            ensureNoXrefIdButLineValue(gr, tagPath, lineI);
            // TODO
          }
          case "HEAD.SOUR.DATA.DATE" -> {
            ensureNoXrefIdButLineValue(gr, tagPath, lineI);
            // TODO
          }
          case "HEAD.SOUR.DATA.COPR" -> {
            ensureNoXrefIdButLineValue(gr, tagPath, lineI);
            // TODO
          }
          case "HEAD.DATE" -> {
            ensureNoXrefIdButLineValue(gr, tagPath, lineI);
            // TODO
          }
          case "HEAD.DATE.TIME" -> {
            ensureNoXrefIdButLineValue(gr, tagPath, lineI);
            // TODO
          }
          case "HEAD.LANG" -> {
            ensureNoXrefIdButLineValue(gr, tagPath, lineI);
            // TODO
          }
          case "HEAD.SUBM" -> {
            ensureNoXrefIdButLineValue(gr, tagPath, lineI);
            // TODO
          }
          case "HEAD.FILE" -> {
            ensureNoXrefIdButLineValue(gr, tagPath, lineI);
            // TODO
          }
          case "HEAD.COPR" -> {
            ensureNoXrefIdButLineValue(gr, tagPath, lineI);
            // TODO
          }
          case "HEAD.NOTE" -> {
            ensureNoXrefIdButLineValue(gr, tagPath, lineI);
            // TODO
          }
          default -> {
            if (recordStack.get(0).tagName().equals("HEAD"))
              throw new ParseException("Unexpected %s tag".formatted(tagPath), lineI);
            else { // End of header reached
              if (!versionFound || !formVersionFound || !sourceFound || encoding == null)
                throw new ParseException("Invalid header", lineI);
              break loop;
            }
          }
        }

        lineI++;
      }
    }

    // Final header checks
    if (recordStack.isEmpty())
      throw new ParseException("Empty file", 0);
    if (encoding == null)
      throw new ParseException("Missing encoding", lineI);

    return new Result(encoding, lineTerminator, lineI);
  }

  private static void ensureValidRecordLevel(final @NotNull Deque<GedcomRecord> recordStack, @NotNull GedcomRecord gr, int lineI) throws ParseException {
    if (recordStack.isEmpty() && gr.level() != 0)
      throw new ParseException("Expected top-level record, got sub-record of level %d".formatted(gr.level()), lineI);
    if (!recordStack.isEmpty() && gr.level() != 0 && gr.level() != recordStack.peek().level() + 1)
      throw new ParseException("Expected top-level record or sub-record of level %d, got sub-record of level %d".formatted(recordStack.peek().level() + 1, gr.level()), lineI);
  }

  private static void ensureNoXrefIdNorLineValue(GedcomRecord gr, String tagPath, int lineI) throws ParseException {
    if (gr.lineValue().isPresent() || gr.xrefId().isPresent())
      throw new ParseException("Invalid %s tag".formatted(tagPath), lineI);
  }

  private static void ensureNoXrefIdButLineValue(@NotNull GedcomRecord gr, @NotNull String tagPath, int lineI) throws ParseException {
    if (gr.xrefId().isPresent() || gr.lineValue().isEmpty())
      throw new ParseException("Invalid %s tag".formatted(tagPath), lineI);
  }

  private static String getTagPath(final @NotNull List<GedcomRecord> recordStack) {
    return recordStack.stream().map(GedcomRecord::tagName).collect(Collectors.joining("."));
  }

  private static @Nullable Pair<String, Optional<String>> readLineWithTerm(@NotNull BufferedReader reader) throws IOException {
    int code;
    final StringBuilder line = new StringBuilder();
    final StringBuilder term = new StringBuilder();

    while ((code = reader.read()) != -1) {
      char ch = (char) code;

      if (ch == '\n') {
        term.append(ch);
        break;
      } else if (ch == '\r') {
        term.append(ch);
        reader.mark(1);
        ch = (char) reader.read();
        if (ch == '\n')
          term.append(ch);
        else
          reader.reset();
        break;
      }

      line.append(ch);
    }

    if (line.isEmpty() && term.isEmpty())
      return null;
    return new Pair<>(
        line.toString(),
        term.isEmpty() ? Optional.empty() : Optional.of(term.toString())
    );
  }

  private static String escape(@NotNull String s) {
    return s.replace("\r", "\\r").replace("\n", "\\n");
  }

  private record Result(@NotNull FileEncoding encoding, @NotNull String lineTerminator, int skipLines) {
    private Result {
      Objects.requireNonNull(encoding);
      Objects.requireNonNull(lineTerminator);
      if (skipLines <= 0)
        throw new IllegalArgumentException("skipLines must be positive");
    }
  }
}
