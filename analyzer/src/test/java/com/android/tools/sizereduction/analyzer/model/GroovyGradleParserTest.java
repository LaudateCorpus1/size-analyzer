/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.android.tools.sizereduction.analyzer.model;

import static com.google.common.truth.Truth.assertThat;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.android.bundle.AppDependenciesOuterClass.Library;
import com.android.bundle.AppDependenciesOuterClass.MavenLibrary;
import com.android.tools.sizereduction.analyzer.utils.TestUtils;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;
import java.io.File;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class GroovyGradleParserTest {

  private static final String PROJECT_BUILD_FILE = "projects/simple_bundle_app/build.gradle";
  private static final String APP_BUILD_FILE = "projects/simple_bundle_app/app/build.gradle";
  private static final String LIBRARY_DEPENDENCY_FILE =
      "gradle_build_files/library_dependency.build.gradle";
  private static final String EMBEDS_WEAR_APK_GRADLE_BUILD_FILE =
      "gradle_build_files/EmbedsWearApk.build.gradle";
  private static final String DYNAMIC_FEATURE_BUILD_FILE =
      "projects/simple_bundle_app/dynamic_feature/build.gradle";
  private static final String PROGUARD_BUILD_FILE =
      "gradle_build_files/proguard_configs.build.gradle";
  private static final String VARIABLE_MINSDK_BUILD_FILE =
      "gradle_build_files/variable_minSdkVersion.build.gradle";
  private static final String VARIABLE_TARGET_BUILD_FILE =
      "gradle_build_files/missing_targetSdkVersion.build.gradle";
  private static final String COMPLEX_BUILD_FILE = "gradle_build_files/complex.build.gradle";
  private static final String TOP_LEVEL_METHOD_CALL_GRADLE_BUILD_FILE =
      "gradle_build_files/top_level_method_call.build.gradle";
  private static final String DISABLE_SPLITS_BUILD_FILE =
      "bundle_configs/disableSplits.build.gradle";
  private static final String MIX_BUNDLE_SPLITS_ENABLED_BUILD_FILE =
      "bundle_configs/splitEnableMix.build.gradle";

  @Test
  public void parsesPluginType_Application() throws Exception {
    File buildFile = TestUtils.getTestDataFile(APP_BUILD_FILE);
    String content = Files.asCharSource(buildFile, UTF_8).read();
    GradleContext context = GroovyGradleParser.parseGradleBuildFile(content, 1, 1, null).build();
    assertThat(context.getPluginType()).isEqualTo(GradleContext.PluginType.APPLICATION);
  }

  @Test
  public void parsesPluginType_DynamicFeature() throws Exception {
    File buildFile = TestUtils.getTestDataFile(DYNAMIC_FEATURE_BUILD_FILE);
    String content = Files.asCharSource(buildFile, UTF_8).read();
    GradleContext context = GroovyGradleParser.parseGradleBuildFile(content, 1, 1, null).build();
    assertThat(context.getPluginType()).isEqualTo(GradleContext.PluginType.DYNAMIC_FEATURE);
  }

  @Test
  public void parsesPluginType_Unknown() throws Exception {
    File buildFile = TestUtils.getTestDataFile(PROJECT_BUILD_FILE);
    String content = Files.asCharSource(buildFile, UTF_8).read();
    GradleContext context = GroovyGradleParser.parseGradleBuildFile(content, 1, 1, null).build();
    assertThat(context.getPluginType()).isEqualTo(GradleContext.PluginType.UNKNOWN);
  }

  @Test
  public void parsesMinSdkVersion() throws Exception {
    File buildFile = TestUtils.getTestDataFile(APP_BUILD_FILE);
    String content = Files.asCharSource(buildFile, UTF_8).read();
    GradleContext context = GroovyGradleParser.parseGradleBuildFile(content, 1, 1, null).build();
    assertThat(context.getMinSdkVersion()).isEqualTo(15);
  }

  @Test
  public void parsesTargetSdkVersion() throws Exception {
    File buildFile = TestUtils.getTestDataFile(APP_BUILD_FILE);
    String content = Files.asCharSource(buildFile, UTF_8).read();
    GradleContext context = GroovyGradleParser.parseGradleBuildFile(content, 1, 1, null).build();
    assertThat(context.getTargetSdkVersion()).isEqualTo(28);
  }

  @Test
  public void parsesMinSdkVersionInTupleExpression() throws Exception {
    File buildFile = TestUtils.getTestDataFile(DYNAMIC_FEATURE_BUILD_FILE);
    String content = Files.asCharSource(buildFile, UTF_8).read();
    GradleContext context = GroovyGradleParser.parseGradleBuildFile(content, 1, 1, null).build();
    assertThat(context.getMinSdkVersion()).isEqualTo(14);
  }

  @Test
  public void parsesProguardConfigs() throws Exception {
    File buildFile = TestUtils.getTestDataFile(PROGUARD_BUILD_FILE);
    String content = Files.asCharSource(buildFile, UTF_8).read();
    GradleContext context = GroovyGradleParser.parseGradleBuildFile(content, 1, 1, null).build();
    ImmutableMap<String, ProguardConfig> expectedMap =
        ImmutableMap.of(
            "release",
            ProguardConfig.builder()
                .setMinifyEnabled(true)
                .setHasProguardRules(true)
                .setObfuscationEnabled(true)
                .build(),
            "shrinkOnly",
            ProguardConfig.builder()
                .setMinifyEnabled(true)
                .setHasProguardRules(true)
                .setObfuscationEnabled(false)
                .build(),
            "debug",
            ProguardConfig.builder()
                .setMinifyEnabled(false)
                .setHasProguardRules(true)
                .setObfuscationEnabled(true)
                .build());

    assertThat(context.getProguardConfigs()).isEqualTo(expectedMap);
  }

  @Test
  public void parsesComplexBuildFile() throws Exception {
    File buildFile = TestUtils.getTestDataFile(COMPLEX_BUILD_FILE);
    String content = Files.asCharSource(buildFile, UTF_8).read();
    GradleContext context = GroovyGradleParser.parseGradleBuildFile(content, 1, 1, null).build();
    ImmutableMap<String, ProguardConfig> expectedMap =
        ImmutableMap.of(
            "release",
            ProguardConfig.builder()
                .setMinifyEnabled(true)
                .setHasProguardRules(true)
                .setObfuscationEnabled(true)
                .build(),
            "debug",
            ProguardConfig.builder()
                .setMinifyEnabled(true)
                .setHasProguardRules(true)
                .setObfuscationEnabled(true)
                .build());

    assertThat(context.getMinSdkVersion()).isEqualTo(19);
    assertThat(context.getPluginType()).isEqualTo(GradleContext.PluginType.APPLICATION);
    assertThat(context.getProguardConfigs()).isEqualTo(expectedMap);
  }

  @Test
  public void variableMinSdkVersionDefaultsToDefaultvalue() throws Exception {
    File buildFile = TestUtils.getTestDataFile(VARIABLE_MINSDK_BUILD_FILE);
    int defaultMinSdkVersion = 345;
    String content = Files.asCharSource(buildFile, UTF_8).read();
    GradleContext context =
        GroovyGradleParser.parseGradleBuildFile(content, defaultMinSdkVersion, 1, null).build();
    assertThat(context.getMinSdkVersion()).isEqualTo(defaultMinSdkVersion);
  }

  @Test
  public void variableTargetSdkVersionDefaultsToDefaultValue() throws Exception {
    File buildFile = TestUtils.getTestDataFile(VARIABLE_TARGET_BUILD_FILE);
    int defaultTargetSdkVersion = 345;
    String content = Files.asCharSource(buildFile, UTF_8).read();
    GradleContext context =
        GroovyGradleParser.parseGradleBuildFile(content, 1, defaultTargetSdkVersion, null).build();
    assertThat(context.getTargetSdkVersion()).isEqualTo(defaultTargetSdkVersion);
  }

  @Test
  public void setsAndroidPluginVersion() throws Exception {
    File buildFile = TestUtils.getTestDataFile(PROJECT_BUILD_FILE);
    String content = Files.asCharSource(buildFile, UTF_8).read();
    GradleContext context = GroovyGradleParser.parseGradleBuildFile(content, 1, 1, null).build();

    assertThat(context.getAndroidPluginVersion()).isNotNull();
    assertThat(context.getAndroidPluginVersion().getMajorVersion()).isEqualTo(3);
    assertThat(context.getAndroidPluginVersion().getMinorVersion()).isEqualTo(4);
  }

  @Test
  public void setsBundleConfigAllFalse() throws Exception {
    File buildFile = TestUtils.getTestDataFile(DISABLE_SPLITS_BUILD_FILE);
    String content = Files.asCharSource(buildFile, UTF_8).read();
    GradleContext context = GroovyGradleParser.parseGradleBuildFile(content, 1, 1, null).build();

    assertThat(context.getBundleConfig().getAbiSplitEnabled()).isFalse();
    assertThat(context.getBundleConfig().getDensitySplitEnabled()).isFalse();
    assertThat(context.getBundleConfig().getLanguageSplitEnabled()).isFalse();
  }

  @Test
  public void setsBundleConfigEnabledByDefault() throws Exception {
    File buildFile = TestUtils.getTestDataFile(APP_BUILD_FILE);
    String content = Files.asCharSource(buildFile, UTF_8).read();
    GradleContext context = GroovyGradleParser.parseGradleBuildFile(content, 1, 1, null).build();

    assertThat(context.getBundleConfig().getAbiSplitEnabled()).isTrue();
    assertThat(context.getBundleConfig().getDensitySplitEnabled()).isTrue();
    assertThat(context.getBundleConfig().getLanguageSplitEnabled()).isTrue();
  }

  @Test
  public void setsBundleConfigMixEnabled() throws Exception {
    File buildFile = TestUtils.getTestDataFile(MIX_BUNDLE_SPLITS_ENABLED_BUILD_FILE);
    String content = Files.asCharSource(buildFile, UTF_8).read();
    GradleContext context = GroovyGradleParser.parseGradleBuildFile(content, 1, 1, null).build();

    assertThat(context.getBundleConfig().getAbiSplitEnabled()).isTrue();
    assertThat(context.getBundleConfig().getDensitySplitEnabled()).isTrue();
    assertThat(context.getBundleConfig().getLanguageSplitEnabled()).isFalse();
  }

  @Test
  public void setsBundleConfigLineNumbersAll() throws Exception {
    File buildFile = TestUtils.getTestDataFile(DISABLE_SPLITS_BUILD_FILE);
    String content = Files.asCharSource(buildFile, UTF_8).read();
    GradleContext context = GroovyGradleParser.parseGradleBuildFile(content, 1, 1, null).build();

    assertThat(context.getBundleConfig().getBundleConfigLocation().getAbiSplitLineNumber())
        .isEqualTo(21);
    assertThat(context.getBundleConfig().getBundleConfigLocation().getDensitySplitLineNumber())
        .isEqualTo(18);
    assertThat(context.getBundleConfig().getBundleConfigLocation().getLanguageSplitLineNumber())
        .isEqualTo(15);
  }

  @Test
  public void setsBundleConfigLineNumberForNone() throws Exception {
    File buildFile = TestUtils.getTestDataFile(APP_BUILD_FILE);
    String content = Files.asCharSource(buildFile, UTF_8).read();
    GradleContext context = GroovyGradleParser.parseGradleBuildFile(content, 1, 1, null).build();

    assertThat(context.getBundleConfig().getBundleConfigLocation().getAbiSplitLineNumber())
        .isNull();
    assertThat(context.getBundleConfig().getBundleConfigLocation().getDensitySplitLineNumber())
        .isNull();
    assertThat(context.getBundleConfig().getBundleConfigLocation().getLanguageSplitLineNumber())
        .isNull();
  }

  @Test
  public void setsBundleConfigLineNumberMixed() throws Exception {
    File buildFile = TestUtils.getTestDataFile(MIX_BUNDLE_SPLITS_ENABLED_BUILD_FILE);
    String content = Files.asCharSource(buildFile, UTF_8).read();
    GradleContext context = GroovyGradleParser.parseGradleBuildFile(content, 1, 1, null).build();

    assertThat(context.getBundleConfig().getBundleConfigLocation().getAbiSplitLineNumber())
        .isNull();
    assertThat(context.getBundleConfig().getBundleConfigLocation().getDensitySplitLineNumber())
        .isEqualTo(18);
    assertThat(context.getBundleConfig().getBundleConfigLocation().getLanguageSplitLineNumber())
        .isEqualTo(15);
  }

  @Test
  public void parseToplevelMethodCall() throws Exception {
    File buildFile = TestUtils.getTestDataFile(TOP_LEVEL_METHOD_CALL_GRADLE_BUILD_FILE);
    String content = Files.asCharSource(buildFile, UTF_8).read();
    GradleContext context = GroovyGradleParser.parseGradleBuildFile(content, 1, 1, null).build();
    assertThat(context).isNotNull();
  }

  @Test
  public void setsLibraryDependencies() throws Exception {
    File buildFile = TestUtils.getTestDataFile(LIBRARY_DEPENDENCY_FILE);
    String content = Files.asCharSource(buildFile, UTF_8).read();
    GradleContext context = GroovyGradleParser.parseGradleBuildFile(content, 1, 1, null).build();

    ImmutableSet<Library> expectedSet =
        ImmutableSet.of(
            Library.newBuilder()
                .setMavenLibrary(
                    MavenLibrary.newBuilder()
                        .setGroupId("com.google.inject")
                        .setArtifactId("guice")
                        .setVersion("3.0")
                        .build())
                .build(),
            Library.newBuilder()
                .setMavenLibrary(
                    MavenLibrary.newBuilder()
                        .setGroupId("com.android.support.constraint")
                        .setArtifactId("constraint-layout")
                        .setVersion("1.1.3")
                        .build())
                .build(),
            Library.newBuilder()
                .setMavenLibrary(
                    MavenLibrary.newBuilder()
                        .setGroupId("com.android.support")
                        .setArtifactId("appcompat-v7")
                        .setVersion("28.0.0")
                        .build())
                .build(),
            Library.newBuilder()
                .setMavenLibrary(
                    MavenLibrary.newBuilder()
                        .setGroupId("com.google.code.guice.test")
                        .setArtifactId("guice")
                        .setVersion("1.0")
                        .build())
                .build(),
            Library.newBuilder()
                .setMavenLibrary(
                    MavenLibrary.newBuilder()
                        .setGroupId("foo")
                        .setArtifactId("bar")
                        .setVersion("1.0.0")
                        .build())
                .build(),
            Library.newBuilder()
                .setMavenLibrary(
                    MavenLibrary.newBuilder()
                        .setGroupId("org.myorg")
                        .setArtifactId("someLib")
                        .setVersion("1.0")
                        .build())
                .build());
    assertThat(context.getLibraryDependencies()).hasSize(6);
    assertThat(context.getLibraryDependencies()).isEqualTo(expectedSet);
  }

  @Test
  public void setsEmbedsWearApk() throws Exception {
    File buildFile = TestUtils.getTestDataFile(EMBEDS_WEAR_APK_GRADLE_BUILD_FILE);
    String content = Files.asCharSource(buildFile, UTF_8).read();
    GradleContext context = GroovyGradleParser.parseGradleBuildFile(content, 1, 1, null).build();

    assertThat(context.getEmbedsWearApk()).isTrue();
  }
}
