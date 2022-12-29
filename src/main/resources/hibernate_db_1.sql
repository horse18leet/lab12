\c hibernate_db
INSERT INTO hibernate_db_schema.customer values (default, 'Istomin'),
                                                (default, 'Kargopoltsev'),
                                                (default, 'Glukhih'),
                                                (default, 'Ryakina'),
                                                (default, 'Mozhegov'),
                                                (default, 'Kanev'),
                                                (default, 'Vorobyov'),
                                                (default, 'Shutov'),
                                                (default, 'Knyazed');

INSERT INTO hibernate_db_schema.product values (default, 'Bananas', 89),
                                               (default, 'Orange', 79),
                                               (default, 'Apples', 79),
                                               (default, 'Orange juice', 139),
                                               (default, 'Cofee', 239),
                                               (default, 'Tea', 99),
                                               (default, 'Meat', 239);

INSERT INTO hibernate_db_schema.customer_product values (default, 10, 1, 2),
                                                        (default, 11, 2, 3),
                                                        (default, 12, 3, 4),
                                                        (default, 13, 4, 5),
                                                        (default, 14, 5, 6),
                                                        (default, 15, 6, 3),
                                                        (default, 16, 7, 2),
                                                        (default, 17, 8, 5);