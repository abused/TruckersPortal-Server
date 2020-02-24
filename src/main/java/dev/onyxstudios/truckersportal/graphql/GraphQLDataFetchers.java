package dev.onyxstudios.truckersportal.graphql;

import com.google.common.collect.Lists;
import com.mongodb.client.MongoCursor;
import graphql.schema.DataFetcher;
import dev.onyxstudios.truckersportal.TruckersPortal;
import dev.onyxstudios.truckersportal.utils.SecurityUtils;
import org.bson.Document;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class GraphQLDataFetchers {

    public DataFetcher getLoadByIdFetcher() {
        return environment -> {
            if(authenticateToken(environment.getArgument("token")) != null) {
                String id = environment.getArgument("id");
                Document filter = new Document("id", id);
                return TruckersPortal.mongoUtils.getDocument("loads", filter);
            }

            return null;
        };
    }

    public DataFetcher getDriverByIdFetcher() {
        return environment -> {
            if(authenticateToken(environment.getArgument("token")) != null) {
                String id = environment.getArgument("id");
                Document filter = new Document("id", id);
                return TruckersPortal.mongoUtils.getDocument("drivers", filter);
            }

            return null;
        };
    }

    public DataFetcher getUserByIdFetcher() {
        return environment -> {
            if(authenticateToken(environment.getArgument("token")) != null) {
                String id = environment.getArgument("id");
                Document filter = new Document("id", id);
                return TruckersPortal.mongoUtils.getDocument("users", filter);
            }

            return null;
        };
    }

    public DataFetcher addLoadFetcher() {
        return environment -> {
            if(authenticateToken(environment.getArgument("token")) != null) {
                Document document = new Document();
                document.append("id", "load-" + Math.floor(100000 + Math.random() * 900000));
                document.append("loadNumber", environment.getArgument("loadNumber"));
                document.append("rate", environment.getArgument("rate"));
                document.append("detention", environment.getArgument("detention"));
                document.append("driverId", environment.getArgument("driverId"));
                document.append("status", environment.getArgument("status"));
                document.append("paid", environment.getArgument("paid"));

                TruckersPortal.mongoUtils.insertDocument("loads", document);
                return document;
            }

            return null;
        };
    }

    public DataFetcher addDriverFetcher() {
        return environment -> {
            if(authenticateToken(environment.getArgument("token")) != null) {
                Document document = new Document();
                document.append("id", "driver-" + Math.floor(100000 + Math.random() * 900000));
                document.append("name", environment.getArgument("name"));
                document.append("payCut", environment.getArgument("payCut"));
                document.append("loadsComplete", new ArrayList<>());
                document.append("phoneNumber", environment.getArgument("phoneNumber"));
                document.append("status", "Sitting");

                TruckersPortal.mongoUtils.insertDocument("drivers", document);
                return document;
            }

            return null;
        };
    }

    public DataFetcher addUserFetcher() {
        return environment -> {
            if(authenticateToken(environment.getArgument("token")) != null) {
                Document document = new Document();
                document.append("id", "user-" + (int) Math.floor(100000 + Math.random() * 900000));
                document.append("firstName", environment.getArgument("firstName"));
                document.append("lastName", environment.getArgument("lastName"));
                document.append("email", environment.getArgument("email"));
                document.append("phoneNumber", environment.getArgument("phoneNumber"));
                document.append("permissions", environment.getArgument("permissions"));
                String hashedPwd = SecurityUtils.hash(environment.getArgument("password").toString().toCharArray());
                document.append("password", hashedPwd);
                String token = SecurityUtils.generateToken();
                document.append("token", token);

                TruckersPortal.mongoUtils.insertDocument("tokens", new Document("token", token));
                TruckersPortal.mongoUtils.insertDocument("users", document);
                return document;
            }

            return null;
        };
    }

    public DataFetcher changeDriverStatus() {
        return environment -> {
            if(authenticateToken(environment.getArgument("token")) != null) {
                String driverId = environment.getArgument("id");
                Document currentData = TruckersPortal.mongoUtils.getDocument("drivers", new Document("id", driverId));
                Document newData = new Document(currentData);
                newData.append("status", environment.getArgument("status"));

                TruckersPortal.mongoUtils.updateDocument("drivers", currentData, newData);
                return newData;
            }

            return null;
        };
    }

    public DataFetcher addDriverLoadFetcher() {
        return environment -> {
            if(authenticateToken(environment.getArgument("token")) != null) {
                String driverId = environment.getArgument("id");
                Document currentData = TruckersPortal.mongoUtils.getDocument("drivers", new Document("id", driverId));
                List<String> loads = currentData.getList("loadsComplete", String.class);
                loads.add(environment.getArgument("loadId"));
                Document newData = new Document(currentData);
                newData.put("loadsComplete", loads);

                TruckersPortal.mongoUtils.updateDocument("drivers", currentData, newData);
                return newData;
            }

            return null;
        };
    }

    public DataFetcher authenticateUserFetcher() {
        return environment -> {
            Document user = TruckersPortal.mongoUtils.getDocument("users", new Document("email", environment.getArgument("email")));
            String password = environment.getArgument("password");
            return SecurityUtils.authenticate(password.toCharArray(), user.getString("password")) ? user : null;
        };
    }

    public DataFetcher addTokenFetcher() {
        return environment -> {
            if(authenticateToken(environment.getArgument("token")) != null) {
                Document document = new Document("token", environment.getArgument("newToken"));
                TruckersPortal.mongoUtils.insertDocument("tokens", document);
                return document;
            }

            return null;
        };
    }

    public DataFetcher updateCarrierFetcher() {
        return environment -> {
            if(authenticateToken(environment.getArgument("token")) != null) {
                Document currentData = TruckersPortal.mongoUtils.getFirstDocument("carrier");
                Document newData = new Document();
                newData.append("name", environment.getArgument("name"));
                newData.append("email", environment.getArgument("email"));
                newData.append("phoneNumber", environment.getArgument("number"));
                newData.append("street", environment.getArgument("street"));
                newData.append("city", environment.getArgument("city"));
                newData.append("state", environment.getArgument("state"));
                newData.append("zipCode", environment.getArgument("zipCode"));
                newData.append("factoring", environment.getArgument("factoring"));
                newData.append("factoringName", environment.getArgument("factoringName"));
                newData.append("factoringStreet", environment.getArgument("factoringStreet"));
                newData.append("factoringCity", environment.getArgument("factoringCity"));
                newData.append("factoringState", environment.getArgument("factoringState"));
                newData.append("factoringZip", environment.getArgument("factoringZip"));

                TruckersPortal.mongoUtils.updateDocument("carrier", currentData, newData);
                return newData;
            }


            return null;
        };
    }

    public DataFetcher getLoadsFetcher() {
        return environment -> {
            if(authenticateToken(environment.getArgument("token")) != null) {
                return Lists.newArrayList(TruckersPortal.mongoUtils.getTableData("loads"));
            }

            return null;
        };
    }

    public DataFetcher getDriversFetcher() {
        return environment -> {
            if(authenticateToken(environment.getArgument("token")) != null) {
                return Lists.newArrayList(TruckersPortal.mongoUtils.getTableData("drivers"));
            }

            return null;
        };
    }

    public DataFetcher getUsersFetcher() {
        return environment -> {
            if(authenticateToken(environment.getArgument("token")) != null) {
                return Lists.newArrayList(TruckersPortal.mongoUtils.getTableData("users"));
            }

            return null;
        };
    }

    public DataFetcher getTotalRevenue() {
        return environment -> {
            if(authenticateToken(environment.getArgument("token")) != null) {
                float revenue = 0;
                for (MongoCursor<Document> it = TruckersPortal.mongoUtils.getTableData("loads"); it.hasNext(); ) {
                    Document load = it.next();
                    if (load.getBoolean("paid"))
                        revenue += load.getDouble("rate") + load.getDouble("detention");
                }

                return new Document("revenue", revenue);
            }

            return null;
        };
    }

    public DataFetcher getUnpaidLoads() {
        return environment -> {
            if(authenticateToken(environment.getArgument("token")) != null) {
                float unpaidRevenue = 0;
                for (MongoCursor<Document> it = TruckersPortal.mongoUtils.getTableData("loads"); it.hasNext(); ) {
                    Document load = it.next();
                    if (!load.getBoolean("paid"))
                        unpaidRevenue += load.getDouble("rate") + load.getDouble("detention");
                }

                return new Document("revenue", unpaidRevenue);
            }

            return null;
        };
    }

    public DataFetcher getCurrentLoads() {
        return environment -> {
            if(authenticateToken(environment.getArgument("token")) != null) {
                int currentLoads = 0;
                for (MongoCursor<Document> it = TruckersPortal.mongoUtils.getTableData("loads"); it.hasNext(); ) {
                    Document load = it.next();
                    if (!load.getString("status").equalsIgnoreCase("Complete"))
                        currentLoads++;
                }

                return new Document("loads", currentLoads);
            }

            return null;
        };
    }

    public DataFetcher getCompletedLoads() {
        return environment -> {
            if(authenticateToken(environment.getArgument("token")) != null) {
                int completedLoads = 0;
                for (MongoCursor<Document> it = TruckersPortal.mongoUtils.getTableData("loads"); it.hasNext(); ) {
                    Document load = it.next();
                    if (load.getString("status").equalsIgnoreCase("Complete"))
                        completedLoads++;
                }

                return new Document("loads", completedLoads);
            }

            return null;
        };
    }

    public DataFetcher authenticateTokenFetcher() {
        return environment -> authenticateToken(environment.getArgument("token"));
    }

    public Document authenticateToken(String token) {
        return TruckersPortal.mongoUtils.getDocument("tokens", new Document("token", token));
    }
}