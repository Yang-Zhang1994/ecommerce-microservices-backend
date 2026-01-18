# Database Initialization

## Overview

This directory contains database initialization scripts and data files. All SQL files in this directory are ready to use:

### SQL Files in this Directory

1. **`01-create-databases.sql`** - Creates all required databases
2. **`gulimall_admin.sql`** - System admin database (tables and initial data)
3. **`gulimall_pms.sql`** - Product Management System database
4. **`gulimall_oms.sql`** - Order Management System database
5. **`gulimall_ums.sql`** - User Management System database
6. **`gulimall_wms.sql`** - Warehouse Management System database
7. **`gulimall_sms.sql`** - System Management Service database
8. **`pms_catelog.sql`** - Product category data (three-level category structure)

Since the project uses MyBatis-Plus, you have two options for database setup:

## Option 1: Auto-create Tables (Recommended for Development)

MyBatis-Plus can automatically create table structures based on entity classes. Add the following configuration to your `application.yml`:

```yaml
mybatis-plus:
  global-config:
    db-config:
      # Auto-create tables (only for development, not recommended for production)
      table-underline: true
      id-type: auto
      logic-delete-value: 1
      logic-not-delete-value: 0
```

**Note**: MyBatis-Plus doesn't have built-in auto-create table feature. You need to:
1. Use a code generator to create tables from entity classes, or
2. Manually create tables based on entity classes, or
3. Use the provided SQL scripts (if available)

## Option 2: Manual Table Creation

1. Create databases first (see `01-create-databases.sql`)
2. Create tables manually based on entity classes in each service module
3. Or use a database migration tool like Flyway or Liquibase

## Database List

The project uses the following databases:

- `gulimall_pms` - Product Management System
- `gulimall_oms` - Order Management System  
- `gulimall_ums` - User Management System
- `gulimall_wms` - Warehouse Management System
- `gulimall_sms` - System Management Service

## Entity Classes Location

Entity classes are located in each service module:
- `gulimall-product/src/main/java/com/xunqi/gulimall/product/entity/`
- `gulimall-order/src/main/java/com/xunqi/gulimall/order/entity/`
- `gulimall-member/src/main/java/com/xunqi/gulimall/member/entity/`
- `gulimall-ware/src/main/java/com/xunqi/gulimall/ware/entity/`
- `gulimall-coupon/src/main/java/com/xunqi/gulimall/coupon/entity/`

You can refer to these entity classes to understand the table structure and create corresponding SQL DDL statements.

## Getting Database Scripts

### Option 1: Extract from db.rar (Recommended)

The project includes `db.rar` in the root directory. Extract it to get complete database initialization scripts:

**On Mac/Linux:**
```bash
# Install unrar if needed: brew install unrar (Mac) or apt-get install unrar (Linux)
unrar x ../db.rar .
```

**On Windows:**
- Use WinRAR or 7-Zip to extract `db.rar` to the `sql/` directory

After extraction, you should have SQL files for each database. All SQL files are already in this `sql/` directory. Import them in order:
1. `01-create-databases.sql` → Create all databases first
2. `gulimall_admin.sql` → System admin database
3. `gulimall_pms.sql` → Product Management System (table structure)
4. `pms_catelog.sql` → Product category data (three-level categories)
5. `gulimall_oms.sql` → Order Management System
6. `gulimall_ums.sql` → User Management System
7. `gulimall_wms.sql` → Warehouse Management System
8. `gulimall_sms.sql` → System Management Service

### Option 2: Use Entity Classes

If you prefer to create tables manually, refer to entity classes in each service module to understand the table structure.

### Import SQL Files

Import SQL files in the following order:

**Step 1: Create databases**
```bash
mysql -u root -p < sql/01-create-databases.sql
```

**Step 2: Import database schemas and data**
```bash
# System admin database
mysql -u root -p gulimall_admin < sql/gulimall_admin.sql

# Product Management System
mysql -u root -p gulimall_pms < sql/gulimall_pms.sql
mysql -u root -p gulimall_pms < sql/pms_catelog.sql  # Three-level category data

# Order Management System
mysql -u root -p gulimall_oms < sql/gulimall_oms.sql

# User Management System
mysql -u root -p gulimall_ums < sql/gulimall_ums.sql

# Warehouse Management System
mysql -u root -p gulimall_wms < sql/gulimall_wms.sql

# System Management Service
mysql -u root -p gulimall_sms < sql/gulimall_sms.sql
```

**Note**: The `pms_catelog.sql` file contains three-level category data for the product management system. Import it after importing `gulimall_pms.sql` to populate the category table with initial data.
