#!/usr/bin/env python3
"""One-page Canadian-style resume with summary and quantified bullets."""

from pathlib import Path

from docx import Document
from docx.oxml import OxmlElement
from docx.oxml.ns import qn
from docx.shared import Inches, Pt, RGBColor

OUTPUT = Path(__file__).resolve().parent.parent / "Yang_Zhang_Resume.docx"

BODY_SIZE = Pt(9)
SECTION_SIZE = Pt(10)
NAME_SIZE = Pt(19)
SUBHEADER_SIZE = Pt(8.5)


def set_run_font(run, size=None, bold=False, color=None, name="Calibri"):
    run.font.name = name
    run.element.rPr.rFonts.set(qn("w:eastAsia"), name)
    if size:
        run.font.size = size
    run.bold = bold
    if color:
        run.font.color.rgb = color


def set_document_style(doc: Document) -> None:
    style = doc.styles["Normal"]
    style.font.name = "Calibri"
    style.font.size = BODY_SIZE
    style.element.rPr.rFonts.set(qn("w:eastAsia"), "Calibri")
    style.paragraph_format.space_after = Pt(0)
    style.paragraph_format.line_spacing = 1.0


def add_section_title(doc: Document, text: str) -> None:
    p = doc.add_paragraph()
    p.paragraph_format.space_before = Pt(4)
    p.paragraph_format.space_after = Pt(1)
    run = p.add_run(text.upper())
    set_run_font(run, SECTION_SIZE, bold=True)
    pPr = p._p.get_or_add_pPr()
    pBdr = pPr.find(qn("w:pBdr"))
    if pBdr is None:
        pBdr = OxmlElement("w:pBdr")
        pPr.append(pBdr)
    bottom = OxmlElement("w:bottom")
    bottom.set(qn("w:val"), "single")
    bottom.set(qn("w:sz"), "4")
    bottom.set(qn("w:space"), "1")
    bottom.set(qn("w:color"), "444444")
    pBdr.append(bottom)


def add_compact_line(doc: Document, text: str, space_after: float = 1) -> None:
    p = doc.add_paragraph(text)
    p.paragraph_format.space_after = Pt(space_after)
    for run in p.runs:
        set_run_font(run, BODY_SIZE)


def add_project_header(doc: Document, title: str, role_dates: str) -> None:
    p = doc.add_paragraph()
    p.paragraph_format.space_before = Pt(2)
    p.paragraph_format.space_after = Pt(0)
    r1 = p.add_run(title)
    set_run_font(r1, BODY_SIZE, bold=True)
    r2 = p.add_run(f"  |  {role_dates}")
    set_run_font(r2, BODY_SIZE)


def add_job_header(doc: Document, title: str, org: str, dates: str) -> None:
    p = doc.add_paragraph()
    p.paragraph_format.space_before = Pt(2)
    p.paragraph_format.space_after = Pt(0)
    r1 = p.add_run(title)
    set_run_font(r1, BODY_SIZE, bold=True)
    r2 = p.add_run(f"  |  {org}  |  {dates}")
    set_run_font(r2, BODY_SIZE)


def add_bullets(doc: Document, items: list[str]) -> None:
    for item in items:
        p = doc.add_paragraph(item, style="List Bullet")
        p.paragraph_format.space_after = Pt(0)
        p.paragraph_format.left_indent = Inches(0.1)
        p.paragraph_format.line_spacing = 1.0
        for run in p.runs:
            set_run_font(run, BODY_SIZE)


def build_resume() -> Document:
    doc = Document()
    set_document_style(doc)

    sec = doc.sections[0]
    sec.top_margin = Inches(0.4)
    sec.bottom_margin = Inches(0.4)
    sec.left_margin = Inches(0.5)
    sec.right_margin = Inches(0.5)

    name_p = doc.add_paragraph()
    set_run_font(name_p.add_run("YANG ZHANG"), NAME_SIZE, bold=True, name="Georgia")

    contact_p = doc.add_paragraph()
    contact_p.paragraph_format.space_after = Pt(2)
    set_run_font(
        contact_p.add_run(
            "Burnaby, BC  |  sc20190702@gmail.com  |  "
            "linkedin.com/in/yang-zhang-527094327  |  github.com/Yang-Zhang1994"
        ),
        SUBHEADER_SIZE,
        color=RGBColor(0x44, 0x44, 0x44),
    )

    add_section_title(doc, "Professional Summary")
    add_compact_line(
        doc,
        "MSCS candidate (GPA 4.0) building production-style distributed systems. Shipped a "
        "12-service e-commerce platform on AWS EKS with live HTTPS storefront, admin portal, "
        "and Stripe checkout; 9 backend unit tests and E2E checkout validation. Strong in "
        "Java/Spring, React/Next.js, PostgreSQL, and Kubernetes/Terraform. Background in "
        "neuroscience research and K-12 stakeholder collaboration. Bilingual: English and Mandarin.",
    )

    add_section_title(doc, "Technical Skills")
    add_compact_line(
        doc,
        "Java, Python, TypeScript, JavaScript, SQL, C  |  Spring Boot 3, Spring Cloud Gateway, "
        "Node.js, Express, Prisma  |  React, Next.js, Vue  |  PostgreSQL, Redis, Elasticsearch, "
        "MongoDB  |  AWS (EKS, ECR, ALB, S3, RDS), Terraform, Helm, Docker, K8s, Git, JUnit, "
        "Mockito, Playwright, Vitest  |  Microservices, RabbitMQ, JWT, OAuth2, Stripe",
    )

    add_section_title(doc, "Software Engineering Projects")

    add_project_header(
        doc,
        "GrainMart — E-Commerce Platform (Microservices)",
        "Full-Stack Developer  |  2024 – Present",
    )
    add_bullets(
        doc,
        [
            "Architected 12 Spring Boot microservices (database-per-service on AWS RDS PostgreSQL) "
            "with Consul discovery and Spring Cloud Gateway; offloaded product search to Elasticsearch; "
            "Redis cache-aside with penetration/breakdown/avalanche guards; idempotent orders and "
            "unique constraints on order numbers and Stripe webhook events.",
            "Delivered Next.js customer mall and Vue admin portal; Stripe Checkout, Google OAuth2, "
            "RabbitMQ async orders, and S3 presigned uploads.",
            "Deployed 12+ microservices on AWS EKS (Terraform, Helm, ALB + ACM HTTPS) across "
            "3 public domains; GitHub Actions CD (unit tests → ECR → Helm atomic deploy); "
            "load-tested product search at ~36 RPS (k6, Gateway/Product HPA); "
            "9 JUnit/Mockito unit tests plus 2 Playwright E2E suites and 10-step API purchase "
            "validation.",
        ],
    )
    add_compact_line(
        doc,
        "Repository: github.com/Yang-Zhang1994/ecommerce-microservices-backend  |  "
        "Live: mall.yangzhangtech.online",
        space_after=0,
    )

    add_project_header(
        doc,
        "SmartChef — Recipe & Ingredient-Matching Web App",
        "Full-Stack Developer  |  2025",
    )
    add_bullets(
        doc,
        [
            "Built React 19 + Node/Express + Prisma/PostgreSQL app with ingredient-match search, "
            "role-based access, and seeded catalog (60+ ingredients, 30 recipes); 8 Vitest "
            "component tests.",
            "Deployed on Vercel + Render with bcrypt auth and HTTP-only JWT cookies "
            "(final-project-kangaroo.vercel.app).",
        ],
    )

    add_project_header(
        doc,
        "BC PhysEd Educational Tool (EdTech)",
        "Backend Developer  |  2025",
    )
    add_bullets(
        doc,
        [
            "Designed REST APIs and a teacher reporting portal for Grades 4–7 LMS; reward-scaling "
            "algorithm driven by real-time student performance (github.com/Yang-Zhang1994/"
            "bc-physed-digital-learning-tool).",
        ],
    )

    add_section_title(doc, "Professional & Research Experience")
    add_job_header(
        doc,
        "Research Assistant (Data-Driven Development)",
        "East China Normal University",
        "2020 – 2023",
    )
    add_bullets(
        doc,
        [
            "Translated neuroscience research into software requirements with K-12 teachers; "
            "managed IRB protocols and coordinated national workshops — experience directly "
            "applied to user-centred API design in EdTech projects.",
            "Led 4 experimental studies; analyzed behavioural datasets with statistical methods "
            "to inform product and learning-design decisions.",
        ],
    )

    add_section_title(doc, "Education")
    add_compact_line(
        doc,
        "Northeastern University — M.S. Computer Science (GPA: 4.0/4.0), 2024 – Present  |  "
        "East China Normal University — M.Ed. Applied Psychology (GPA: 3.61/4.0), 2018 – 2021  |  "
        "Central China Normal University — B.Sc. Chemistry, 2013 – 2017",
        space_after=0,
    )

    return doc


def main() -> None:
    doc = build_resume()
    doc.save(OUTPUT)
    print(f"Saved: {OUTPUT}")


if __name__ == "__main__":
    main()
