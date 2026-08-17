# Object-Oriented Programming (OOP) and GRASP Patterns Analysis Report

This report provides a detailed analysis of the usage of OOP principles (Inheritance, Composition, Aggregation) and GRASP (General Responsibility Assignment Software Patterns) within the `d:\UND` repository. 

---

## 1. Object-Oriented Programming Principles

### 1.1 Inheritance
Inheritance is used to establish an "is-a" relationship between classes, promoting code reusability and establishing hierarchical classifications.

*   **File:** `d:\UND\src\main\java\com\aegisrx\domain\Admin.java`
    *   **Class/Entity:** `Admin extends User`
    *   **Line Number:** 6
    *   **Explanation:** The `Admin` class inherits common properties (like username, password) and behaviors from the base `User` class.
*   **File:** `d:\UND\src\main\java\com\aegisrx\domain\Manufacturer.java`
    *   **Class/Entity:** `Manufacturer extends User`
    *   **Line Number:** 6
    *   **Explanation:** Similar to Admin, `Manufacturer` extends the generic `User` class to represent a specific type of user with its own specific roles.
*   **File:** `d:\UND\src\main\java\com\aegisrx\domain\Patient.java`
    *   **Class/Entity:** `Patient extends User`
    *   **Line Number:** 8
    *   **Explanation:** Represents a patient user, inheriting base authentication/identity attributes from `User`.
*   **File:** `d:\UND\src\main\java\com\aegisrx\domain\PharmacyStaff.java`
    *   **Class/Entity:** `PharmacyStaff extends User`
    *   **Line Number:** 6
    *   **Explanation:** Represents a pharmacy staff member, inheriting core attributes from the `User` base class.
*   **File:** `d:\UND\src\main\java\com\aegisrx\ui\MainApp.java`
    *   **Class/Entity:** `MainApp extends Application`
    *   **Line Number:** 9
    *   **Explanation:** The main entry point of the JavaFX application, inheriting from the base JavaFX `Application` class to define UI lifecycle behaviors.

### 1.2 Polymorphism & Interfaces (Implementation)
Interfaces are implemented to define contracts that multiple classes can fulfill, a core aspect of polymorphism.

*   **File:** `d:\UND\src\main\java\com\aegisrx\ai\AIInteractionChecker.java`
    *   **Class/Entity:** `AIInteractionChecker implements InteractionCheckStrategy`
    *   **Line Number:** 12
    *   **Explanation:** Implements a specific strategy for checking AI interactions.
*   **File:** `d:\UND\src\main\\java\com\aegisrx\ai\LocalInteractionChecker.java`
    *   **Class/Entity:** `LocalInteractionChecker implements InteractionCheckStrategy`
    *   **Line Number:** 10
    *   **Explanation:** Provides an alternative, local implementation for the interaction checking strategy.
*   **File:** `d:\UND\src\main\java\com\aegisrx\dao\DatabaseHandler.java`
    *   **Class/Entity:** `DatabaseHandler implements PersistenceHandler`
    *   **Line Number:** 4
    *   **Explanation:** Implements standard persistence operations for a relational database.
*   **File:** `d:\UND\src\main\java\com\\aegisrx\dao\FileHandler.java`
    *   **Class/Entity:** `FileHandler implements PersistenceHandler`
    *   **Line Number:** 9
    *   **Explanation:** Implements the same persistence contract but handles local file I/O operations.
*   **File:** `d:\UND\src\main\java\com\aegisrx\observer\CaretakerNotificationObserver.java`
    *   **Class/Entity:** `CaretakerNotificationObserver implements AlertObserver`
    *   **Line Number:** 6
    *   **Explanation:** Implements an observer to receive notification updates for caretakers.
*   **File:** `d:\UND\src\main\java\com\aegisrx\observer\UIAlertObserver.java`
    *   **Class/Entity:** `UIAlertObserver implements AlertObserver`
    *   **Line Number:** 10
    *   **Explanation:** Acts as a UI-level observer that updates views when an alert event is published.

### 1.3 Composition & Aggregation
Composition/Aggregation represents a "has-a" relationship, where complex objects are built from simpler ones.

*   **File:** `d:\UND\src\main\java\com\aegisrx\dao\DatabaseConnectionManager.java`
    *   **Field/Entity:** `private Connection connection;`
    *   **Line Number:** 11
    *   **Explanation:** The DatabaseConnectionManager encapsulates a database `Connection` object (Composition) to manage database state securely.
*   **File:** `d:\UND\src\main\java\com\aegisrx\domain\DietaryAlert.java`
    *   **Field/Entity:** `private AlertSeverity severity;`
    *   **Line Number:** 13
    *   **Explanation:** A dietary alert "has-a" severity level.
*   **File:** `d:\UND\src\main\java\com\aegisrx\domain\InteractionReport.java`
    *   **Field/Entity:** `private AlertSeverity severity;`
    *   **Line Number:** 12
    *   **Explanation:** An interaction report aggregates a severity enum object.
*   **File:** `d:\UND\src\main\java\com\aegisrx\domain\MedicineBatch.java`
    *   **Field/Entity:** `private BatchStatus status;`
    *   **Line Number:** 17
    *   **Explanation:** A batch "has-a" particular batch status associated with it.
*   **File:** `d:\UND\src\main\java\com\aegisrx\domain\Patient.java`
    *   **Field/Entity:** `private List<Medication> activeMedications;`
    *   **Line Number:** 11
    *   **Explanation:** A Patient object aggregates multiple `Medication` objects, indicating the medications currently prescribed (Aggregation).
*   **File:** `d:\UND\src\main\java\com\aegisrx\domain\User.java`
    *   **Field/Entity:** `private UserRole role;`
    *   **Line Number:** 13
    *   **Explanation:** A user entity is composed of a specific role, separating role behaviors from the user profile.
*   **File:** `d:\UND\src\main\java\com\aegisrx\service\DrugInteractionService.java`
    *   **Field/Entity:** `private InteractionCheckStrategy strategy;`
    *   **Line Number:** 16
    *   **Explanation:** The service delegates interaction checks to a `strategy` object. This implements composition for swapping algorithms at runtime (Strategy Pattern).

### 1.4 Design Patterns
These patterns represent generalized solutions to common problems in software design, utilized within the project.

*   **Singleton Pattern**
    *   **File:** `d:\UND\src\main\java\com\aegisrx\dao\DatabaseConnectionManager.java`
    *   **Class/Entity:** `DatabaseConnectionManager`
    *   **Line Number:** 10, 21
    *   **Explanation:** The `getInstance()` method ensures that only a single instance of the database connection manager is created, providing a global point of access and avoiding resource leaks.
*   **Factory Pattern**
    *   **File:** `d:\UND\src\main\java\com\aegisrx\dao\PersistenceHandlerFactory.java`
    *   **Class/Entity:** `PersistenceHandlerFactory`
    *   **Line Number:** 10
    *   **Explanation:** The `createHandler()` method implements the Factory pattern, hiding the instantiation logic and returning a concrete `PersistenceHandler` (like `DatabaseHandler` or `FileHandler`) based on the application configuration.

---

## 2. GRASP Patterns

GRASP (General Responsibility Assignment Software Patterns) is a set of guidelines for assigning responsibilities to classes and objects.

### 2.1 Controller
The Controller pattern assigns the responsibility of dealing with system events to a non-UI class that represents the overall system or a use case scenario.

*   **AdminDashboardController** (`d:\UND\src\main\java\com\aegisrx\ui\controllers\AdminDashboardController.java` - Line 1)
*   **ManufacturerDashboardController** (`d:\UND\src\main\java\com\aegisrx\ui\controllers\ManufacturerDashboardController.java` - Line 1)
*   **PatientDashboardController** (`d:\UND\src\main\java\com\aegisrx\ui\controllers\PatientDashboardController.java` - Line 1)
*   **PharmacyDashboardController** (`d:\UND\src\main\java\com\aegisrx\ui\controllers\PharmacyDashboardController.java` - Line 1)
*   **RoleSelectorController** (`d:\UND\src\main\java\com\aegisrx\ui\controllers\RoleSelectorController.java` - Line 1)
    *   **Explanation:** These Controller classes are the first to receive user input actions from the UI layer. They coordinate workflows and delegate business tasks to the backend `Services`.

### 2.2 Creator
The Creator pattern dictates which class is responsible for creating a new instance of a given class.

*   **PersistenceHandlerFactory** (`d:\UND\src\main\java\com\aegisrx\dao\PersistenceHandlerFactory.java` - Line 1)
    *   **Explanation:** Implements the Factory pattern (a manifestation of Creator). It dictates how persistence handlers (Database vs. File) are instantiated based on configuration.
*   **GBIGenerator** (`d:\UND\src\main\java\com\aegisrx\util\GBIGenerator.java` - Line 1)
    *   **Explanation:** Responsible for creating/generating GBI objects or tokens.

### 2.3 Information Expert & High Cohesion
Information Expert suggests placing responsibilities in classes that possess the required information. High Cohesion indicates that classes should be tightly focused on a single logical group of operations.

#### Data Access Objects (DAOs) - Responsible for database persistence logic
*   **BatchDAO** (`d:\UND\src\main\java\com\aegisrx\dao\BatchDAO.java` - Line 1)
*   **DietaryAlertDAO** (`d:\UND\src\main\java\com\aegisrx\dao\DietaryAlertDAO.java` - Line 1)
*   **DrugProfileDAO** (`d:\UND\src\main\java\com\aegisrx\dao\DrugProfileDAO.java` - Line 1)
*   **FoodEntryDAO** (`d:\UND\src\main\java\com\aegisrx\dao\FoodEntryDAO.java` - Line 1)
*   **MedicationDAO** (`d:\UND\src\main\java\com\aegisrx\dao\MedicationDAO.java` - Line 1)
*   **PharmacyDAO** (`d:\UND\src\main\java\com\aegisrx\dao\PharmacyDAO.java` - Line 1)
*   **SaleDAO** (`d:\UND\src\main\java\com\aegisrx\dao\SaleDAO.java` - Line 1)
*   **UserDAO** (`d:\UND\src\main\java\com\aegisrx\dao\UserDAO.java` - Line 1)
    *   **Explanation:** Each DAO represents High Cohesion by strictly handling only the Create, Read, Update, and Delete operations for its respective domain model. They act as the Information Experts for database queries.

#### Services - Responsible for business logic rules
*   **AIService** (`d:\UND\src\main\java\com\aegisrx\service\AIService.java` - Line 1)
*   **BatchService** (`d:\UND\src\main\java\com\aegisrx\service\BatchService.java` - Line 1)
*   **DietaryConflictService** (`d:\UND\src\main\java\com\aegisrx\service\DietaryConflictService.java` - Line 1)
*   **DrugInteractionService** (`d:\UND\src\main\java\com\aegisrx\service\DrugInteractionService.java` - Line 1)
*   **InventoryService** (`d:\UND\src\main\java\com\aegisrx\service\InventoryService.java` - Line 1)
*   **MedicationProfileService** (`d:\UND\src\main\java\com\aegisrx\service\MedicationProfileService.java` - Line 1)
*   **SaleService** (`d:\UND\src\main\java\com\aegisrx\service\SaleService.java` - Line 1)
*   **SmartLocatorService** (`d:\UND\src\main\java\com\aegisrx\service\SmartLocatorService.java` - Line 1)
*   **UserService** (`d:\UND\src\main\java\com\aegisrx\service\UserService.java` - Line 1)
    *   **Explanation:** Each service encapsulates all domain-specific logic and rules for its given context, shielding the Controllers from complex validation logic and enforcing High Cohesion.

### 2.4 Polymorphism
Assigns responsibilities to interfaces to create indirect paths to handle varying behaviors, decoupled from explicit dependencies.

*   **CaretakerNotificationObserver** (`d:\UND\src\main\java\com\aegisrx\observer\CaretakerNotificationObserver.java` - Line 6)
*   **UIAlertObserver** (`d:\UND\src\main\java\com\aegisrx\observer\UIAlertObserver.java` - Line 10)
    *   **Explanation:** Follows the Observer pattern where polymorphism is used to define a common interface (`AlertObserver`). This provides a mechanism to notify decoupled system components simultaneously without tight coupling.

### 2.5 Indirection
The Indirection pattern assigns the responsibility of mediating between other components to an intermediate object so that they are not directly coupled.

*   **DrugInteractionService** (`d:\UND\src\main\java\com\aegisrx\service\DrugInteractionService.java` - Line 16)
    *   **Explanation:** Acts as an intermediary between the UI controllers and the interaction checking strategies (`InteractionCheckStrategy`), removing direct dependencies.

### 2.6 Low Coupling
Low Coupling dictates that classes should have minimal dependencies on each other to increase resilience to change.

*   **InteractionCheckStrategy** (`d:\UND\src\main\java\com\aegisrx\ai\InteractionCheckStrategy.java`)
    *   **Explanation:** By depending on this interface instead of concrete classes like `AIInteractionChecker`, the `DrugInteractionService` maintains low coupling.

### 2.7 Pure Fabrication
Pure Fabrication involves creating a class that does not represent a concept in the problem domain, specially made to achieve low coupling, high cohesion, or reuse potential.

*   **GBIGenerator** (`d:\UND\src\main\java\com\aegisrx\util\GBIGenerator.java` - Line 6)
    *   **Explanation:** A utility class created purely to handle the generation of unique IDs (Global Batch Identifiers). It does not represent a real-world healthcare domain object but is necessary for system mechanics.

### 2.8 Protected Variations
Protected Variations protects elements from the variations on other elements (objects, systems, subsystems) by wrapping the focus of instability with an interface and using polymorphism to create various implementations.

*   **PersistenceHandlerFactory** (`d:\UND\src\main\java\com\aegisrx\dao\PersistenceHandlerFactory.java` - Line 6)
    *   **Explanation:** The rest of the application is protected from variations in data storage mechanisms (Database vs. File system). By using this factory, clients operate on the abstract `PersistenceHandler`, unaware of the underlying changes.

---
*Note: This report only includes code definitions and patterns found within the `d:\UND` workspace and its direct subdirectories, as requested.*
