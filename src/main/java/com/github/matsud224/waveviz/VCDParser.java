package com.github.matsud224.waveviz;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Optional;

public final class VCDParser {
    public static class InvalidVCDFormatException extends Exception {
        public InvalidVCDFormatException(String message) {
            super(message);
        }
    }

    public static class MetaData {
        private String comment;
        private String date;
        private String version;
        private String timeScale;

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getTimeScale() {
            return timeScale;
        }

        public void setTimeScale(String timeScale) {
            this.timeScale = timeScale;
        }
    }

    private static class ParserInternalData {
        private final HierarchyTree hierarchy;
        private final HashMap<String, ValueChangeStore> storeMap;
        private final MetaData metaData;

        private ParserInternalData(HierarchyTree hierarchy, HashMap<String, ValueChangeStore> storeMap, MetaData metaData) {
            this.hierarchy = hierarchy;
            this.storeMap = storeMap;
            this.metaData = metaData;
        }

        public HierarchyTree getHierarchy() {
            return hierarchy;
        }

        public HashMap<String, ValueChangeStore> getStoreMap() {
            return storeMap;
        }

        public MetaData getMetaData() {
            return metaData;
        }
    }

    public static class ParseResult {
        private final HierarchyTree hierarchy;
        private final MetaData metaData;

        private ParseResult(HierarchyTree hierarchy, MetaData metaData) {
            this.hierarchy = hierarchy;
            this.metaData = metaData;
        }

        public HierarchyTree getHierarchy() {
            return hierarchy;
        }

        public MetaData getMetaData() {
            return metaData;
        }
    }

    private enum DeclarationKeyword {
        COMMENT, DATE, ENDDEFINITIONS, SCOPE, TIMESCALE, UPSCOPE, VAR, VERSION
    }

    private enum SimulationKeyword {
        COMMENT, DUMPALL, DUMPOFF, DUMPON, DUMPVARS
    }

    private enum ScopeType {
        BEGIN, FORK, FUNCTION, MODULE, TASK
    }

    private enum TimeNumber {
        ONE, TEN, THOUSAND
    }

    private enum TimeUnit {
        S, MS, US, NS, PS, FS
    }

    private enum VarType {
        EVENT, INTEGER, PARAMETER, REAL, REALTIME, REG, SUPPLY0, SUPPLY1, TIME,
        TRI, TRIAND, TRIOR, TRIREG, TRI0, TRI1, WAND, WIRE, WOR
    }

    public static ParseResult parse(Reader reader, String name) throws IOException, InvalidVCDFormatException {
        var internalData = parseDeclarationCommands(new PushbackReader(reader, 1024), name);
        var lastTime = parseSimulationCommands(internalData, new PushbackReader(reader, 1024));
        internalData.storeMap.forEach((k, v) -> v.addChange(lastTime, null));
        return new ParseResult(internalData.getHierarchy(), internalData.getMetaData());
    }

    private static ParserInternalData parseDeclarationCommands(PushbackReader reader, String name)
            throws IOException, InvalidVCDFormatException {
        var metaDataStore = new MetaData();
        var storeMap = new HashMap<String, ValueChangeStore>();
        var root = new HierarchyTree(name, "FILE", null);
        var currentScope = root;
        while (true) {
            var kwOpt = parseDeclarationKeyword(reader);
            if (kwOpt.isEmpty())
                break;
            switch (kwOpt.get()) {
                case COMMENT:
                    var commentStr = consumeUntilEnd(reader);
                    metaDataStore.setComment(commentStr);
                    break;
                case DATE:
                    var dateStr = consumeUntilEnd(reader);
                    metaDataStore.setDate(dateStr);
                    break;
                case ENDDEFINITIONS:
                    return new ParserInternalData(root, storeMap, metaDataStore);
                case SCOPE:
                    var scopeType = parseScopeType(reader).orElseThrow(() -> new InvalidVCDFormatException("expected scope type of $scope"));
                    var scopeIdentifier = readWord(reader).orElseThrow(() -> new InvalidVCDFormatException("expected scope identifier of $scope"));
                    consumeEnd(reader);
                    var scope = new HierarchyTree(scopeIdentifier, scopeType.toString(), currentScope);
                    currentScope.children.add(scope);
                    currentScope = scope;
                    break;
                case TIMESCALE:
                    var timeNumber = parseTimeNumber(reader).orElseThrow(() -> new InvalidVCDFormatException("expected time number of $timescale"));
                    var timeUnit = parseTimeUnit(reader).orElseThrow(() -> new InvalidVCDFormatException("expected time unit of $timescale"));
                    consumeEnd(reader);
                    System.out.printf("Timescale: %s %s\n", timeNumber.toString(), timeUnit.toString());
                    break;
                case UPSCOPE:
                    consumeEnd(reader);
                    currentScope = currentScope.getParent();
                    break;
                case VAR:
                    var varType = parseVarType(reader).orElseThrow(() -> new InvalidVCDFormatException("expected var type of $var"));
                    var size = Integer.parseInt(readWord(reader).orElseThrow(() -> new InvalidVCDFormatException("expected size of $var")));
                    var identifier = readWord(reader).orElseThrow(() -> new InvalidVCDFormatException("expected identifier of $var"));
                    var reference = getReference(reader);
                    consumeEnd(reader);

                    var store = storeMap.get(identifier);
                    if (store == null) {
                        store = new ValueChangeStore();
                        storeMap.put(identifier, store);
                    }

                    var path = currentScope.getPath();
                    path.add(reference);
                    var signal = new Signal(path, varType.toString(), size, store);
                    currentScope.signals.add(signal);
                    break;
                case VERSION:
                    var versionStr = consumeUntilEnd(reader);
                    metaDataStore.setVersion(versionStr);
                    break;
            }
        }
        throw new InvalidVCDFormatException("no declaration commands found");
    }

    private static String getReference(PushbackReader reader) throws InvalidVCDFormatException, IOException {
        var identifier = readWord(reader).orElseThrow(() -> new InvalidVCDFormatException("expected identifier of reference"));

        skipWhitespaces(reader);
        var c = reader.read();
        if (c != '[') {
            if (c != -1) reader.unread(c);
            return identifier;
        }
        var index0 = readDecimalNumber(reader).orElseThrow(() -> new InvalidVCDFormatException("expected index of reference"));

        skipWhitespaces(reader);
        switch (reader.read()) {
            case ':':
                break;
            case ']':
                return identifier + '[' + index0 + ']';
            default:
                throw new InvalidVCDFormatException("invalid index of reference");
        }
        var index1 = readDecimalNumber(reader).orElseThrow(() -> new InvalidVCDFormatException("expected 2nd index of reference"));

        skipWhitespaces(reader);
        if (reader.read() != ']')
            throw new InvalidVCDFormatException("invalid index of reference");

        return identifier + '[' + index0 + ':' + index1 + ']';
    }

    private static boolean parseValueChange(PushbackReader reader, int time, ParserInternalData internalData) throws IOException, InvalidVCDFormatException {
        skipWhitespaces(reader);
        var c = reader.read();
        if (c == -1)
            return false;
        switch (c) {
            case '0':
            case '1':
            case 'x':
            case 'X':
            case 'z':
            case 'Z': {
                var idCode = readWord(reader).orElseThrow(() -> new InvalidVCDFormatException("expected identifier code of value change"));
                //System.out.printf("Scalar change: %s to %c\n", idCode, c);
                var store = internalData.storeMap.get(idCode);
                if (store == null) {
                    System.out.printf("Warning: ignoring unknown identifier code %s\n", idCode);
                    System.exit(-1);
                } else {
                    store.addChange(time, Character.toString(c));
                }
                return true;
            }
            case 'b':
            case 'B':
            case 'r':
            case 'R': {
                var numStr = readWord(reader).orElseThrow(() -> new InvalidVCDFormatException("expected number of value change"));
                var idCode = readWord(reader).orElseThrow(() -> new InvalidVCDFormatException("expected identifier code of value change"));
                //System.out.printf("Vector change: %s to %s\n", idCode, numStr);
                var store = internalData.storeMap.get(idCode);
                if (store == null) {
                    System.out.printf("Warning: ignoring unknown identifier code %s\n", idCode);
                    System.exit(-1);
                } else {
                    store.addChange(time, numStr);
                }
                return true;
            }
            default:
                reader.unread(c);
                return false;
        }
    }

    private static void skipWhitespaces(PushbackReader reader) throws IOException {
        while (true) {
            var c = reader.read();
            if (!Character.isWhitespace(c)) {
                if (c != -1) reader.unread(c);
                break;
            }
        }
    }

    private static int parseSimulationCommands(ParserInternalData internalData, PushbackReader reader)
            throws IOException, InvalidVCDFormatException {
        int time = 0;
        while (true) {
            var kwOpt = parseSimulationKeyword(reader);
            if (kwOpt.isPresent()) {
                switch (kwOpt.get()) {
                    case COMMENT:
                        consumeUntilEnd(reader);
                        break;
                    case DUMPALL:
                    case DUMPOFF:
                    case DUMPON:
                    case DUMPVARS:
                        while (true) {
                            if (!parseValueChange(reader, time, internalData)) {
                                break;
                            }
                        }
                        consumeEnd(reader);
                        break;
                }
            } else {
                if (!parseValueChange(reader, time, internalData)) {
                    int c = reader.read();
                    if (c == -1) {
                        break;
                    } else if (c == '#') {
                        time = readDecimalNumber(reader).orElseThrow(() -> new InvalidVCDFormatException("expected time after #"));
                    }
                }
            }
        }
        return time;
    }

    private static String consumeUntilEnd(PushbackReader reader) throws IOException {
        var sb = new StringBuilder();
        while (true) {
            Optional<String> wopt = readWord(reader);
            if (wopt.isEmpty()) {
                break;
            } else {
                if (wopt.get().equals("$end"))
                    break;
                if (sb.length() != 0)
                    sb.append(' ');
                sb.append(wopt.get());
            }
        }
        return sb.toString();
    }

    private static void consumeEnd(PushbackReader reader) throws IOException, InvalidVCDFormatException {
        Optional<String> wopt = readWord(reader);
        if (!(wopt.isPresent() && wopt.get().equals("$end"))) {
            throw new InvalidVCDFormatException("expected $end");
        }
    }

    private static Optional<String> readWord(PushbackReader reader) throws IOException {
        var sb = new StringBuilder(256);
        skipWhitespaces(reader);
        while (true) {
            int c = reader.read();
            if (c != -1 && !Character.isWhitespace(c)) {
                sb.append((char) c);
            } else {
                if (c != -1) reader.unread(c);
                break;
            }
        }
        if (sb.length() == 0)
            return Optional.empty();
        else
            return Optional.of(sb.toString());
    }

    private static Optional<Integer> readDecimalNumber(PushbackReader reader) throws IOException {
        var sb = new StringBuilder(256);
        skipWhitespaces(reader);
        while (true) {
            int c = reader.read();
            if (Character.isDigit(c)) {
                sb.append((char) c);
            } else {
                if (c != -1) reader.unread(c);
                break;
            }
        }
        if (sb.length() == 0)
            return Optional.empty();
        else
            return Optional.of(Integer.parseInt(sb.toString()));
    }

    private static Optional<DeclarationKeyword> parseDeclarationKeyword(PushbackReader reader) throws IOException {
        var wopt = readWord(reader);
        if (wopt.isEmpty()) {
            return Optional.empty();
        } else {
            switch (wopt.get()) {
                case "$comment":
                    return Optional.of(DeclarationKeyword.COMMENT);
                case "$date":
                    return Optional.of(DeclarationKeyword.DATE);
                case "$enddefinitions":
                    return Optional.of(DeclarationKeyword.ENDDEFINITIONS);
                case "$scope":
                    return Optional.of(DeclarationKeyword.SCOPE);
                case "$timescale":
                    return Optional.of(DeclarationKeyword.TIMESCALE);
                case "$upscope":
                    return Optional.of(DeclarationKeyword.UPSCOPE);
                case "$var":
                    return Optional.of(DeclarationKeyword.VAR);
                case "$version":
                    return Optional.of(DeclarationKeyword.VERSION);
                default:
                    reader.unread(' ');
                    reader.unread(wopt.get().toCharArray());
                    reader.unread(' ');
                    return Optional.empty();
            }
        }
    }

    private static Optional<SimulationKeyword> parseSimulationKeyword(PushbackReader reader) throws IOException {
        var wopt = readWord(reader);
        if (wopt.isEmpty()) {
            return Optional.empty();
        } else {
            switch (wopt.get()) {
                case "$comment":
                    return Optional.of(SimulationKeyword.COMMENT);
                case "$dumpall":
                    return Optional.of(SimulationKeyword.DUMPALL);
                case "$dumpoff":
                    return Optional.of(SimulationKeyword.DUMPOFF);
                case "$dumpon":
                    return Optional.of(SimulationKeyword.DUMPON);
                case "$dumpvars":
                    return Optional.of(SimulationKeyword.DUMPVARS);
                default:
                    reader.unread(' ');
                    reader.unread(wopt.get().toCharArray());
                    reader.unread(' ');
                    return Optional.empty();
            }
        }
    }

    private static Optional<ScopeType> parseScopeType(PushbackReader reader) throws IOException {
        var wopt = readWord(reader);
        if (wopt.isEmpty()) {
            return Optional.empty();
        } else {
            switch (wopt.get()) {
                case "begin":
                    return Optional.of(ScopeType.BEGIN);
                case "fork":
                    return Optional.of(ScopeType.FORK);
                case "function":
                    return Optional.of(ScopeType.FUNCTION);
                case "module":
                    return Optional.of(ScopeType.MODULE);
                case "task":
                    return Optional.of(ScopeType.TASK);
                default:
                    reader.unread(' ');
                    reader.unread(wopt.get().toCharArray());
                    reader.unread(' ');
                    return Optional.empty();
            }
        }
    }

    private static Optional<TimeNumber> parseTimeNumber(PushbackReader reader) throws IOException {
        skipWhitespaces(reader);
        var c = reader.read();
        if (c != '1') {
            if (c != -1) reader.unread(c);
            return Optional.empty();
        }
        c = reader.read();
        if (c != '0') {
            if (c != -1) reader.unread(c);
            return Optional.of(TimeNumber.ONE);
        }
        c = reader.read();
        if (c != '0') {
            if (c != -1) reader.unread(c);
            return Optional.of(TimeNumber.TEN);
        }
        return Optional.of(TimeNumber.THOUSAND);
    }

    private static Optional<TimeUnit> parseTimeUnit(PushbackReader reader) throws IOException {
        var wopt = readWord(reader);
        if (wopt.isEmpty()) {
            return Optional.empty();
        } else {
            switch (wopt.get()) {
                case "s":
                    return Optional.of(TimeUnit.S);
                case "ms":
                    return Optional.of(TimeUnit.MS);
                case "us":
                    return Optional.of(TimeUnit.US);
                case "ns":
                    return Optional.of(TimeUnit.NS);
                case "ps":
                    return Optional.of(TimeUnit.PS);
                case "fs":
                    return Optional.of(TimeUnit.FS);
                default:
                    reader.unread(' ');
                    reader.unread(wopt.get().toCharArray());
                    reader.unread(' ');
                    return Optional.empty();
            }
        }
    }

    private static Optional<VarType> parseVarType(PushbackReader reader) throws IOException {
        var wopt = readWord(reader);
        if (wopt.isEmpty()) {
            return Optional.empty();
        } else {
            switch (wopt.get()) {
                case "event":
                    return Optional.of(VarType.EVENT);
                case "integer":
                    return Optional.of(VarType.INTEGER);
                case "parameter":
                    return Optional.of(VarType.PARAMETER);
                case "real":
                    return Optional.of(VarType.REAL);
                case "realtime":
                    return Optional.of(VarType.REALTIME);
                case "reg":
                    return Optional.of(VarType.REG);
                case "supply0":
                    return Optional.of(VarType.SUPPLY0);
                case "supply1":
                    return Optional.of(VarType.SUPPLY1);
                case "time":
                    return Optional.of(VarType.TIME);
                case "tri":
                    return Optional.of(VarType.TRI);
                case "triand":
                    return Optional.of(VarType.TRIAND);
                case "trior":
                    return Optional.of(VarType.TRIOR);
                case "trireg":
                    return Optional.of(VarType.TRIREG);
                case "tri0":
                    return Optional.of(VarType.TRI0);
                case "tri1":
                    return Optional.of(VarType.TRI1);
                case "wand":
                    return Optional.of(VarType.WAND);
                case "wire":
                    return Optional.of(VarType.WIRE);
                case "wor":
                    return Optional.of(VarType.WOR);
                default:
                    reader.unread(' ');
                    reader.unread(wopt.get().toCharArray());
                    reader.unread(' ');
                    return Optional.empty();
            }
        }
    }
}