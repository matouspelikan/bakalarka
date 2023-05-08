import java.util.*;
import java.util.stream.Collectors;

/**
 * jedinec v populaci
 */
public class Individual {

    public static Config config;
    public static Random random;

    public List<Edge> priorityList;
    public Evaluation evaluation;


    public Individual(List<Edge> priorityList){
        this.priorityList = Main.deepCopy(priorityList);
    }

    /**
     * pouze pro BestSoFar ucely, potrebuji pouze evaluation pro porovnani,
     * prioriotyList a evaluation.routes me nezajimaji, jsou null
     */
    public int nbofGeneration;
    public Individual(Individual individual, int nbofGeneration){
        this.evaluation = new Evaluation(individual.evaluation);
        this.nbofGeneration = nbofGeneration;
    }
    public Individual(){
        this.evaluation = new Evaluation(Double.POSITIVE_INFINITY, Integer.MAX_VALUE, null);
        this.nbofGeneration = Integer.MAX_VALUE;
    }

    public void evaluate(JournalPair journalPair, boolean journaling){
        this.evaluation = Main.evaluatePriorityList(priorityList, config, journalPair, journaling);
    }

    public void perturb(JournalPair journalPair, boolean journaling){
        Collections.shuffle(this.priorityList, new Random(random.nextInt()));
        this.evaluate(journalPair, journaling);
    }

    public Object toHash(Element element){
        if (element != null){
            return element.candidate.edge.hash();
        }
        return 0;
    }

    public int hashCustom(){
        int hash = 0;
        for(Route route : evaluation.routes){
            Element el = route.tail;
            while(el != null){

                Object previousHash = toHash(el.previous);
                Object nextHash = toHash(el.next);

                Set<Object> set = new HashSet<>(Arrays.asList(el.previousLink.hash(),
                        el.candidate.edge.hash(), el.nextLink.hash(), previousHash, nextHash));
                hash += set.hashCode();

                el = el.next;
            }
        }

        return hash;
    }

    public List<Edge> crossWith(Individual other){
        List<Edge> newPriorityList = new ArrayList<>();
        int half = priorityList.size() / 2;
        for (int i = 0; i < half; i++) {
            newPriorityList.add(priorityList.get(i));
        }

        for (int i = newPriorityList.size(); i < priorityList.size(); i++) {
            for (int j = 0; j < other.priorityList.size(); j++) {
                if(!newPriorityList.contains(other.priorityList.get(j))){
                    newPriorityList.add(other.priorityList.get(j));
                    break;
                }
            }
        }

        return newPriorityList;
    }

    public List<Edge> crossWith2(Individual other){
        int midsize = this.priorityList.size()/2;
        int residuum = priorityList.size() - midsize;
        int presize = residuum/2;
        int postsize = residuum - presize;

        if(presize + midsize + postsize != priorityList.size()) throw new RuntimeException();

        List<Edge> newPriorityList = new ArrayList<>();
        for (int i = presize; i < presize + midsize; i++) {
            newPriorityList.add(priorityList.get(i));
        }

        for (int i = 0; i < presize; i++) {
            for (int j = 0; j < other.priorityList.size(); j++) {
                if(!newPriorityList.contains(other.priorityList.get(j))){
                    newPriorityList.add(0, other.priorityList.get(j));
                    break;
                }
            }
        }
        for (int i = 0; i < postsize; i++) {
            for (int j = 0; j < other.priorityList.size(); j++) {
                if(!newPriorityList.contains(other.priorityList.get(j))){
                    newPriorityList.add(other.priorityList.get(j));
                    break;
                }
            }
        }

        if(newPriorityList.size() != priorityList.size()){
            throw new RuntimeException();
        }

        return newPriorityList;
    }

    public String printRoutes(){
        String s = "";
        for(Route r : evaluation.routes){
            Element el = r.tail;
            while(el != null){
//                System.out.print(el);
                s += el.toString();
                el = el.next;
            }
//            System.out.println();
            s += '\n';
        }
        return s;
    }

    public void mutate(){
        int indexFrom = random.nextInt(priorityList.size());
        int indexTo = random.nextInt(priorityList.size());
        Edge temp = priorityList.get(indexFrom);

        priorityList.set(indexFrom, priorityList.get(indexTo));
        priorityList.set(indexTo, temp);

//        System.out.println(indexFrom + "  " + indexTo);
    }

    //greedy localOptim, pokud nalezne lokalni zlepseni, provede ho ihned
    public void localOptimisation(JournalPair journalPair){

        for(Route r : evaluation.routes){
            r.twoOptWrap();
            r.singleInsertWrap();
            r.singleReverseWrap();
        }

        singleInsertMultipleWrap(this);
        twoOptMultipleWrap(this);


        for (int j = 0; j < 10; j++) {
//            for (int l = 2; l < 4; l++) {
//                this.pathScanningWrap(journal, l);
//            }
            this.pathScanningWrap(journalPair, 2);
        }
//        this.pathScanningWrap(journal, 2);


//        for(Route r : evaluation.routes){
////            System.out.println(r.length());
//            if(r.lengthReverse() != r.length()) throw new RuntimeException();
//        }

    }

    public void pathScanningWrap(JournalPair journalPair, int limit){
        List<Route> routes = evaluation.routes;
        Collections.shuffle(routes, new Random(random.nextInt()));
        List<Route> subRoutes = routes.stream().filter(r -> r.active).limit(limit).collect(Collectors.toList());
        double pre = Main.evaluateRoutes(subRoutes, config);
        Evaluation evaluationLocal = pathScanning(subRoutes, journalPair);
        if(evaluationLocal.cost < pre && evaluationLocal.vehicleCount <= evaluation.vehicleCount){
            int rpre = Main.evaluateRoutes(routes, config);
            for(Route r : subRoutes){
                routes.remove(r);
            }
            routes.addAll(evaluationLocal.routes);
            int rpost = Main.evaluateRoutes(routes, config);

            evaluation.cost = rpost;
            evaluation.vehicleCount = routes.size();

            if(pre - evaluationLocal.cost != rpre - rpost){
                throw new RuntimeException();
            }
        }
//        pathScanning(routes.stream().limit(3).collect(Collectors.toList()));
    }

    public Evaluation pathScanning(List<Route> routes, JournalPair journalPair){
        List<Edge> allEdges = new ArrayList<>();
        for (Route r : routes){
            Element element = r.tail;
            while(element != null){
                allEdges.add(new Edge(element.candidate.edge));
                element = element.next;
            }
        }
        Collections.shuffle(allEdges, new Random(random.nextInt()));
        return Main.evaluatePriorityList(allEdges, config, journalPair, false);
    }

    public static void twoOptMultipleWrap(Individual individual){
        List<Route> routes = individual.evaluation.routes;

        double diff;
        for (int i = 0; i < routes.size(); i++) {
            Route route = routes.get(i);
            int len = route.length();
            for (int j = 0; j < routes.size(); j++) {
                if (i == j) continue;
                Route otherRoute = routes.get(j);
                int otherRouteLength = otherRoute.length();
                for (int k = 0; k < len; k++) {
                    for (int l = 0; l < otherRouteLength; l++) {
                        int routeLen = route.length();
                        int otherRouteLen = otherRoute.length();
                        if(k >= routeLen) break;
                        if(l >= otherRouteLen) break;
                        if(k == routeLen - 1 && l == otherRouteLen - 1) break;

//                        System.out.println(k + " " + route.length() + " | " + l + " " + otherRouteLength);
                        if((diff = twoOptMultiple(route, k, otherRoute, l)) < 0){
//                            System.out.println("twoOptMultiple diff " + diff);

//                            Element r1Head = route.head;
//                            Element r2Head = otherRoute.head;

                            int demandBefore = route.demand() + otherRoute.demand();
                            double evalBefore = Main.evaluateRoute(route, config.matrix) + Main.evaluateRoute(otherRoute, config.matrix);

                            twoOptMultipleApply(route, k, otherRoute, l);

                            int demandAfter = route.demand() + otherRoute.demand();
                            double evalAfter = Main.evaluateRoute(route, config.matrix) + Main.evaluateRoute(otherRoute, config.matrix);

                            if(demandBefore != demandAfter) throw new RuntimeException();
                            if(evalAfter != evalBefore + diff) throw new RuntimeException();

//                            System.out.println(route.head + " " + r2Head);
//                            System.out.println(otherRoute.head + " " + r1Head);


//                            throw new RuntimeException();
                        }
                    }
                }
            }
        }
    }

    public static double getSubDemand(Element e){
        double demand = 0;
        while(e != null){
            demand += e.candidate.edge.demand;
            e = e.next;
        }
        return demand;
    }

    public static double getSubDemandReverse(Element e){
        double demand = 0;
        while(e != null){
            demand += e.candidate.edge.demand;
            e = e.previous;
        }
        return demand;
    }


    public static double twoOptMultiple(Route r1, int index1, Route r2, int index2){
        Element e1 = r1.get(index1);
        Element e2 = r2.get(index2);

        double d1 = getSubDemandReverse(e1);
        double d12 = getSubDemand(e1.next);

        double d2 = getSubDemandReverse(e2);
        double d22 = getSubDemand(e2.next);

        if(d1 + d22 > config.capacity || d12 + d2 > config.capacity){
            return 1.0;
        }

        double diff = - e1.nextDistance - e2.nextDistance;

        int e1N = Route.elementToNumberNext(e1.next);
        int e2N = Route.elementToNumberNext(e2.next);

        diff += config.matrix[e1.nextLink.number][e2N];
        diff += config.matrix[e2.nextLink.number][e1N];

        return diff;
    }

    public static void twoOptMultipleApply(Route r1, int index1, Route r2, int index2) {
        Element e1 = r1.get(index1);
        Element e2 = r2.get(index2);

//        System.out.println("routes eval: " + Main.evaluateRoute(r1, matrix2) + " " + Main.evaluateRoute(r2, matrix2));
//        System.out.println("demands: " + r1.demand() + " " + r2.demand());

        Element e1Next = e1.next;
        Element e2Next = e2.next;

        Element r1Head = r1.head;

        double d1 = getSubDemandReverse(e1);
        double d12 = getSubDemand(e1.next);

        double d2 = getSubDemandReverse(e2);
        double d22 = getSubDemand(e2.next);

//        if(d1 + d22 > config.capacity || d12 + d2 > config.capacity){
//            return 1.0;
//        }

        r1.capacityTaken -= d12;
        r1.capacityLeft += d12;
        e1.next = null;
        r1.head = e1;

        if(e2Next == null){
            e1.nextDistance = config.matrix[e1.nextLink.number][1];
        }
        else{
            e2Next.previous = null;

            Route newRoute = new Route();
            newRoute.tail = e2Next;
            newRoute.head = r2.head;

            newRoute.capacityTaken = (int) d22;
            newRoute.capacityLeft = config.capacity - newRoute.capacityTaken;

            Element it = newRoute.tail;
            while(it != null){
                it.candidate.edge.component = newRoute;
                it = it.next;
            }

            Candidate candidate = new Candidate(e2Next.candidate.edge, e2Next.previousLink, e1.nextLink, config.matrix[e1.nextLink.number][e2Next.previousLink.number]);
//            r1.mergeRouteF(candidate, e2Next);

            r1.mergeRouteE(candidate);
        }

        r2.capacityTaken -= d22;
        r2.capacityLeft += d22;
        e2.next = null;
        r2.head = e2;

        if(e1Next == null){
            e2.nextDistance = config.matrix[e2.nextLink.number][1];
        }
        else{
            e1Next.previous = null;

            Route newRoute = new Route();
            newRoute.tail = e1Next;
            newRoute.head = r1Head;

            newRoute.capacityTaken = (int)d12;
            newRoute.capacityLeft = config.capacity - newRoute.capacityTaken;

            Element it = newRoute.tail;
            while(it != null){
                it.candidate.edge.component = newRoute;
                it = it.next;
            }

            Candidate candidate = new Candidate(e1Next.candidate.edge, e1Next.previousLink, e2.nextLink, config.matrix[e1Next.previousLink.number][e2.nextLink.number]);

//            r2.mergeRouteF(candidate, e1Next);
            r2.mergeRouteE(candidate);
        }
//        System.out.println("routes eval: " + Main.evaluateRoute(r1, matrix2) + " " + Main.evaluateRoute(r2, matrix2));
//        System.out.println("demands: " + r1.demand() + " " + r2.demand());
//

//        throw new RuntimeException();

    }

    public static void singleInsertMultipleWrap(Individual individual){
        List<Route> routes = individual.evaluation.routes;

        double diff;
        for (int i = 0; i < routes.size(); i++) {
            Route route = routes.get(i);
            int len = route.length();
            for (int j = 0; j < routes.size(); j++) {
                if (i == j) continue;
                Route otherRoute = routes.get(j);
                int otherRouteLength = otherRoute.length();
                for (int k = 0; k < len; k++) {
                    for (int l = 0; l < otherRouteLength; l++) {
                        if(k >= route.length()) break;
                        if(l >= otherRoute.length()) break;
                        if(!route.active || !otherRoute.active){
                            System.out.println("ahaaaa");
                        }
//                        System.out.println(k + " " + route.length() + " | " + l + " " + otherRouteLength);
                        if((diff = singleInsertMultiple(route, k, otherRoute, l)) < 0){
//                            System.out.println("singleInsertMultiple diff " + diff + " | " + k + " " + len + " | " + l + " " + otherRouteLength);

                            double r1before = Main.evaluateRoute(route, config.matrix);
                            double r2before = Main.evaluateRoute(otherRoute, config.matrix);

                            singleInsertMultipleApply(route, k, otherRoute, l);

                            double r1after = Main.evaluateRoute(route, config.matrix);
                            double r2after = Main.evaluateRoute(otherRoute, config.matrix);

                            if(r1after + r2after != r1before + r2before + diff) throw new RuntimeException();
                        }
                    }
                }
            }
        }


        List<Route> routesToDelete = new ArrayList<>();
        for(Route r : routes){
            if(!r.active){
                routesToDelete.add(r);
            }
        }

        individual.evaluation.routes.removeAll(routesToDelete);
        individual.evaluation.vehicleCount = individual.evaluation.routes.size();
    }

    public static double singleInsertMultiple(Route r1, int index1, Route r2, int index2){
        Element e1 = r1.get(index1);
        Element e2 = r2.get(index2);

        if(r2.capacityLeft < e1.candidate.edge.demand){
            return 1.0;
        }

        double diff = - e1.previousDistance - e1.nextDistance;
        int e1P = Route.elementToNumberPrev(e1.previous);
        int e1N = Route.elementToNumberNext(e1.next);
        diff += config.matrix[e1P][e1N];

        diff -= e2.nextDistance;
        diff += config.matrix[e2.nextLink.number][e1.previousLink.number];
        int e2N = Route.elementToNumberNext(e2.next);
        diff += config.matrix[e1.nextLink.number][e2N];

        return diff;
    }

    public static void singleInsertMultipleApply(Route r1, int index1, Route r2, int index2){
        Element e1 = r1.get(index1);
        Element e2 = r2.get(index2);

        Element e1Prev = e1.previous;
        Element e1Next = e1.next;

        if(e1Prev != null){
            e1.previous.next = e1Next;
            int e1N = Route.elementToNumberNext(e1Next);
            e1.previous.nextDistance = config.matrix[e1.previous.nextLink.number][e1N];
        }
        else{
            r1.tail = e1Next;
            if(r1.tail == null){ //TODO route zaníká, asi početřeno
                r1.active = false;
            }
        }

        if(e1Next != null){
            e1.next.previous = e1Prev;
            int e1P = Route.elementToNumberPrev(e1Prev);
            e1.next.previousDistance = config.matrix[e1.next.previousLink.number][e1P];
        }
        else{
            r1.head = e1.previous;
        }


        //TODO doublecheck
        Candidate candidate = new Candidate(e1.candidate.edge, e1.previousLink, e2.nextLink, config.matrix[e2.nextLink.number][e1.previousLink.number]);
        candidate.edge.component = r2;
        e1.candidate = candidate;
//        e1.candidate.edge.component = r2;

        Element e2Prev = e2.previous;
        Element e2Next = e2.next;

        e2.next = e1;
        e2.nextDistance = config.matrix[e2.nextLink.number][e1.previousLink.number];

        e1.previous = e2;
        e1.previousDistance = e2.nextDistance;

        if(e2Next != null){
            e2Next.previous = e1;
            e2Next.previousDistance = config.matrix[e2Next.previousLink.number][e1.nextLink.number];
        }
        else{
            r2.head = e1;
        }
        e1.next = e2Next;
        int e2N = Route.elementToNumberNext(e2Next);
        e1.nextDistance = config.matrix[e1.nextLink.number][e2N];

        r2.capacityTaken += e1.candidate.edge.demand;
        r2.capacityLeft -= e1.candidate.edge.demand;

        r1.capacityTaken -= e1.candidate.edge.demand;
        r1.capacityLeft += e1.candidate.edge.demand;
    }




    @Override
    public String toString() {
        return evaluation.toString() + " " + nbofGeneration;
    }
}
