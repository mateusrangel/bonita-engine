package com.bonitasoft.engine.business.data.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.InputStream;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.transaction.UserTransaction;

import org.assertj.core.data.Index;
import org.bonitasoft.engine.dependency.DependencyService;
import org.bonitasoft.engine.dependency.model.SDependency;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.dbunit.DataSourceDatabaseTester;
import org.dbunit.IDatabaseTester;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import bitronix.tm.TransactionManagerServices;
import bitronix.tm.resource.jdbc.PoolingDataSource;

import com.bonitasoft.engine.business.data.NonUniqueResultException;
import com.bonitasoft.engine.business.data.SBusinessDataNotFoundException;
import com.bonitasoft.pojo.Employee;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/testContext.xml" })
public class JPABusinessDataRepositoryImplIT {

    private IDatabaseTester databaseTester;

    private JPABusinessDataRepositoryImpl businessDataRepository;

    @Mock
    private DependencyService dependencyService;

    @Mock
    private TechnicalLoggerService loggerService;

    @Autowired
    @Qualifier("businessDataDataSource")
    private PoolingDataSource datasource;

    @Resource(name = "jpa-configuration")
    private Map<String, Object> configuration;

    @BeforeClass
    public static void initializeBitronix() throws NamingException, SQLException {
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "bitronix.tm.jndi.BitronixInitialContextFactory");
        TransactionManagerServices.getConfiguration().setJournal(null);
    }

    @AfterClass
    public static void shutdownTransactionManager() {
        TransactionManagerServices.getTransactionManager().shutdown();
    }

    @Before
    public void setUp() throws Exception {
        initMocks(this);

        businessDataRepository = spy(new JPABusinessDataRepositoryImpl(dependencyService, loggerService, configuration));
        doReturn(null).when(businessDataRepository).createSDependency(anyLong(), any(byte[].class));
        doReturn(null).when(businessDataRepository).createDependencyMapping(anyLong(), any(SDependency.class));
        doReturn(true).when(businessDataRepository).isDBMDeployed();
    }

    @After
    public void tearDown() throws Exception {
        if (databaseTester != null) {
            final UserTransaction ut = TransactionManagerServices.getTransactionManager();
            try {
                ut.begin();
                databaseTester.setTearDownOperation(DatabaseOperation.DELETE_ALL);
                databaseTester.onTearDown();
            } catch (final Exception e) {
                ut.rollback();
            } finally {
                ut.commit();
            }
        }
    }

    public void setUpDatabase() throws Exception {
        databaseTester = new DataSourceDatabaseTester(datasource);
        final InputStream stream = JPABusinessDataRepositoryImplIT.class.getResourceAsStream("/dataset.xml");
        final FlatXmlDataSet dataSet = new FlatXmlDataSetBuilder().build(stream);
        stream.close();
        databaseTester.setDataSet(dataSet);
        databaseTester.setSetUpOperation(DatabaseOperation.CLEAN_INSERT);
        databaseTester.onSetup();
    }

    @Test
    public void findAnEmployeeByPrimaryKey() throws Exception {
        executeInTransaction(new RunnableInTransaction(true) {

            @Override
            public void run() {
                try {
                    final Employee employee = businessDataRepository.findById(Employee.class, 45l);
                    assertThat(employee).isNotNull();
                    assertThat(employee.getPersistenceId()).isEqualTo(45l);
                    assertThat(employee.getFirstName()).isEqualTo("Hannu");
                    assertThat(employee.getLastName()).isEqualTo("Hakkinen");
                } catch (final Exception e) {
                    throw new RuntimeException(e);
                }
            }

        });
    }

    @Test(expected = SBusinessDataNotFoundException.class)
    public void throwExceptionWhenEmployeeNotFound() throws Exception {
        executeInTransaction(new RunnableInTransaction(false) {

            @Override
            public void run() {
                try {
                    businessDataRepository.findById(Employee.class, -145l);
                } catch (final Exception e) {
                    throw new RuntimeException(e);
                }
            }

        });
    }

    @Test
    public void persistNewEmployee() throws Exception {
        executeInTransaction(new RunnableInTransaction(false) {

            @Override
            public void run() {
                try {
                    final Employee employee = businessDataRepository.merge(new Employee("Marja", "Halonen"));
                    assertThat(employee.getPersistenceId()).isNotNull();
                } catch (final Exception e) {
                    throw new RuntimeException(e);
                }
            }

        });
    }

    @Test
    public void persistANullEmployee() throws Exception {
        executeInTransaction(new RunnableInTransaction(false) {

            @Override
            public void run() {
                try {
                    businessDataRepository.merge(null);
                    final Long count = businessDataRepository.find(Long.class, "SELECT COUNT(*) FROM Employee e", null);
                    assertThat(count).isEqualTo(0);
                } catch (final Exception e) {
                    throw new RuntimeException(e);
                }
            }

        });
    }

    @Test
    public void findAnEmployeeUsingParameterizedQuery() throws Exception {
        executeInTransaction(new RunnableInTransaction(true) {

            @Override
            public void run() {
                final Map<String, Object> parameters = Collections.singletonMap("firstName", (Object) "Matti");
                try {
                    final Employee matti = businessDataRepository.find(Employee.class, "FROM Employee e WHERE e.firstName = :firstName", parameters);
                    assertThat(matti.getFirstName()).isEqualTo("Matti");
                } catch (final Exception e) {
                    throw new RuntimeException(e);
                }
            }

        });
    }

    @Test(expected = NonUniqueResultException.class)
    public void throwExceptionWhenFindingAnEmployeeButGettingSeveral() throws Exception {
        executeInTransaction(new RunnableInTransaction(true) {

            @Override
            public void run() {
                final Map<String, Object> parameters = Collections.singletonMap("lastName", (Object) "Hakkinen");
                try {
                    businessDataRepository.find(Employee.class, "FROM Employee e WHERE e.lastName = :lastName", parameters);
                } catch (final Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @Test(expected = SBusinessDataNotFoundException.class)
    public void throwExceptionWhenFindingAnUnknownEmployee() throws Exception {
        executeInTransaction(new RunnableInTransaction(true) {

            @Override
            public void run() {
                final Map<String, Object> parameters = Collections.singletonMap("lastName", (Object) "Unknown_lastName");
                try {
                    businessDataRepository.find(Employee.class, "FROM Employee e WHERE e.lastName = :lastName", parameters);
                } catch (final Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @Test(expected = IllegalStateException.class)
    public void throwExceptionWhenUsingBDRWihtoutStartingIt() throws Exception {
        final UserTransaction ut = TransactionManagerServices.getTransactionManager();
        try {
            ut.begin();
            final Map<String, Object> parameters = Collections.singletonMap("lastName", (Object) "Makkinen");
            businessDataRepository.find(Employee.class, "FROM Employee e WHERE e.lastName = :lastName", parameters);
        } finally {
            ut.commit();
        }
    }

    @Test
    public void entityClassNames_is_an_empty_set_if_bdr_is_not_started() throws Exception {

        final Set<String> classNames = businessDataRepository.getEntityClassNames();

        assertThat(classNames).isEmpty();
    }

    @Test
    public void entityClassNames_contains_all_entities_class_names() throws Exception {
        final UserTransaction ut = TransactionManagerServices.getTransactionManager();
        try {
            ut.begin();
            businessDataRepository.start();

            final Set<String> classNames = businessDataRepository.getEntityClassNames();

            assertThat(classNames).containsOnly("com.bonitasoft.pojo.Employee");
        } finally {
            ut.commit();
        }
    }

    @Test
    public void updateTwoFieldsInSameTransactionShouldModifySameObject() throws Exception {
        UserTransaction ut = TransactionManagerServices.getTransactionManager();
        try {
            ut.begin();
            businessDataRepository.start();
            setUpDatabase();
            final Map<String, Object> parameters = Collections.singletonMap("firstName", (Object) "Matti");
            final Employee matti = businessDataRepository.find(Employee.class, "FROM Employee e WHERE e.firstName = :firstName", parameters);
            matti.setLastName("NewLastName");
            businessDataRepository.merge(matti);
            matti.setFirstName("NewFirstName");
            businessDataRepository.merge(matti);
        } finally {
            ut.commit();
        }

        ut = TransactionManagerServices.getTransactionManager();
        try {
            ut.begin();
            final Map<String, Object> parameters = Collections.singletonMap("firstName", (Object) "NewFirstName");
            final Employee matti = businessDataRepository.find(Employee.class, "FROM Employee e WHERE e.firstName = :firstName", parameters);
            assertThat(matti.getLastName()).isEqualTo("NewLastName");
        } finally {
            ut.commit();
            businessDataRepository.stop();
        }
    }

    @Test
    public void updateAnEmployeeUsingParameterizedQuery() throws Exception {
        UserTransaction ut = TransactionManagerServices.getTransactionManager();
        try {
            ut.begin();
            businessDataRepository.start();
            setUpDatabase();
            final Map<String, Object> parameters = Collections.singletonMap("firstName", (Object) "Matti");
            final Employee matti = businessDataRepository.find(Employee.class, "FROM Employee e WHERE e.firstName = :firstName", parameters);
            matti.setLastName("Halonen");
            businessDataRepository.merge(matti);
        } finally {
            ut.commit();
        }

        ut = TransactionManagerServices.getTransactionManager();
        try {
            ut.begin();
            final Map<String, Object> parameters = Collections.singletonMap("firstName", (Object) "Matti");
            final Employee matti = businessDataRepository.find(Employee.class, "FROM Employee e WHERE e.firstName = :firstName", parameters);
            assertThat(matti.getLastName()).isEqualTo("Halonen");
        } finally {
            ut.commit();
            businessDataRepository.stop();
        }
    }

    /**
     * Sets up the database (if specified), the businessDataRepository, and runs a piece of business logics inside a transaction.
     * 
     * @param runnable
     *            the logics to run.
     */
    private void executeInTransaction(final RunnableInTransaction runnable) throws Exception {
        final UserTransaction ut = TransactionManagerServices.getTransactionManager();
        try {
            ut.begin();
            businessDataRepository.start();
            if (runnable.isSetupDatabase()) {
                setUpDatabase();
            }

            runnable.run();
        } catch (final RuntimeException e) {
            throw (Exception) e.getCause();
        } finally {
            ut.commit();
            businessDataRepository.stop();
        }

    }

    private static abstract class RunnableInTransaction implements Runnable {

        private final boolean setupDatabase;

        public RunnableInTransaction(final boolean setupDatabase) {
            this.setupDatabase = setupDatabase;
        }

        public boolean isSetupDatabase() {
            return setupDatabase;
        }
    }

    @Test
    public void getEntityClassNames_returns_the_classes_managed_by_the_bdr() throws Exception {
        executeInTransaction(new RunnableInTransaction(true) {

            @Override
            public void run() {
                final Set<String> expected = Collections.singleton(Employee.class.getName());

                final Set<String> entityClassNames = businessDataRepository.getEntityClassNames();
                assertThat(entityClassNames).isEqualTo(expected);
            }
        });
    }

    @Test(expected = SBusinessDataNotFoundException.class)
    public void removeAnEntity() throws Exception {
        UserTransaction ut = TransactionManagerServices.getTransactionManager();
        try {
            ut.begin();
            businessDataRepository.start();
            setUpDatabase();
            final Map<String, Object> parameters = Collections.singletonMap("firstName", (Object) "Matti");
            final Employee matti = businessDataRepository.find(Employee.class, "FROM Employee e WHERE e.firstName = :firstName", parameters);
            businessDataRepository.remove(matti);
        } finally {
            ut.commit();
        }

        ut = TransactionManagerServices.getTransactionManager();
        try {
            ut.begin();
            final Map<String, Object> parameters = Collections.singletonMap("firstName", (Object) "Matti");
            businessDataRepository.find(Employee.class, "FROM Employee e WHERE e.firstName = :firstName", parameters);
            fail("The employee was removed previously");
        } finally {
            ut.commit();
            businessDataRepository.stop();
        }
    }

    @Test
    public void remove_should_not_throw_an_exception_with_a_null_entity() throws Exception {
        executeInTransaction(new RunnableInTransaction(true) {

            @Override
            public void run() {
                businessDataRepository.remove(null);
            }
        });
    }

    @Test
    public void remove_should_not_throw_an_exception_with_an_unknown_entity_without_an_id() throws Exception {
        executeInTransaction(new RunnableInTransaction(true) {

            @Override
            public void run() {
                businessDataRepository.remove(new Employee("Tarja", "Makkinen"));
            }
        });
    }

    @Test
    public void remove_should_not_throw_an_exception_with_an_unknown_entity() throws Exception {
        Employee matti;
        UserTransaction ut = TransactionManagerServices.getTransactionManager();
        try {
            ut.begin();
            businessDataRepository.start();
            setUpDatabase();
            final Map<String, Object> parameters = Collections.singletonMap("firstName", (Object) "Matti");
            matti = businessDataRepository.find(Employee.class, "FROM Employee e WHERE e.firstName = :firstName", parameters);
            businessDataRepository.remove(matti);
        } finally {
            ut.commit();
        }

        ut = TransactionManagerServices.getTransactionManager();
        try {
            ut.begin();
            businessDataRepository.remove(matti);
        } catch (final Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            ut.commit();
            businessDataRepository.stop();
        }
    }

    @Test
    public void findList_should_return_employee_list() throws Exception {
        final UserTransaction ut = TransactionManagerServices.getTransactionManager();
        try {
            ut.begin();
            businessDataRepository.start();
            setUpDatabase();
            final List<Employee> employees = businessDataRepository.findList(Employee.class, "SELECT e FROM Employee e ORDER BY e.lastName, e.firstName", null);
            assertThat(employees).hasSize(3).has(new EmployeeCondition("Hannu", "Hakkinen"), Index.atIndex(0))
                    .has(new EmployeeCondition("Matti", "Hakkinen"), Index.atIndex(1)).has(new EmployeeCondition("Petteri", "Salo"), Index.atIndex(2));

        } finally {
            ut.commit();
        }
    }

    @Test
    public void findList_should_return_an_empty() throws Exception {
        final UserTransaction ut = TransactionManagerServices.getTransactionManager();
        try {
            ut.begin();
            businessDataRepository.start();
            setUpDatabase();
            final Map<String, Object> parameters = Collections.singletonMap("firstName", (Object) "Jaakko");
            final List<Employee> employees = businessDataRepository.findList(Employee.class,
                    "SELECT e FROM Employee e WHERE e.firstName=:firstName ORDER BY e.lastName, e.firstName", parameters);
            assertThat(employees).isEmpty();

        } finally {
            ut.commit();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void findList_should_throw_an_exception_if_parameter_is_not_set() throws Exception {
        final UserTransaction ut = TransactionManagerServices.getTransactionManager();
        try {
            ut.begin();
            businessDataRepository.start();
            setUpDatabase();
            businessDataRepository.findList(Employee.class, "SELECT e FROM Employee e WHERE e.firstName=:firstName ORDER BY e.lastName, e.firstName", null);
        } finally {
            ut.commit();
        }
    }

}
